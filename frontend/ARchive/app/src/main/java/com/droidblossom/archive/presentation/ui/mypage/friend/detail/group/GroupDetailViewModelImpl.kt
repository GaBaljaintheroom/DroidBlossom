package com.droidblossom.archive.presentation.ui.mypage.friend.detail.group

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.droidblossom.archive.data.dto.common.IdBasedPagingRequestDto
import com.droidblossom.archive.data.dto.common.PagingRequestDto
import com.droidblossom.archive.domain.model.group.GroupMember
import com.droidblossom.archive.domain.model.group.toGroupProfileData
import com.droidblossom.archive.domain.usecase.group.DeleteGroupUseCase
import com.droidblossom.archive.domain.usecase.group.GetGroupDetailUseCase
import com.droidblossom.archive.domain.usecase.group.LeaveGroupUseCase
import com.droidblossom.archive.domain.usecase.group_capsule.CapsulesOfGroupPageUseCase
import com.droidblossom.archive.presentation.base.BaseViewModel
import com.droidblossom.archive.presentation.model.mypage.CapsuleData
import com.droidblossom.archive.presentation.model.mypage.detail.GroupProfileData
import com.droidblossom.archive.util.DateUtils
import com.droidblossom.archive.util.onException
import com.droidblossom.archive.util.onFail
import com.droidblossom.archive.util.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModelImpl @Inject constructor(
    private val getGroupDetailUseCase: GetGroupDetailUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val capsulesOfGroupPageUseCase: CapsulesOfGroupPageUseCase
) : BaseViewModel(), GroupDetailViewModel {

    private val _groupDetailEvents = MutableSharedFlow<GroupDetailViewModel.GroupDetailEvent>()
    override val groupDetailEvents: SharedFlow<GroupDetailViewModel.GroupDetailEvent>
        get() = _groupDetailEvents.asSharedFlow()

    private val _groupId = MutableStateFlow(-1L)
    override val groupId: StateFlow<Long>
        get() = _groupId

    private val _groupMembers = MutableStateFlow(listOf<GroupMember>())
    override val groupMembers: StateFlow<List<GroupMember>>
        get() = _groupMembers

    private val _capsules = MutableStateFlow(listOf<CapsuleData>())
    override val capsules: StateFlow<List<CapsuleData>>
        get() = _capsules

    private val _capsulesHasNextPage = MutableStateFlow(true)
    override val capsulesHasNextPage: StateFlow<Boolean>
        get() = _capsulesHasNextPage

    private val _isShowMore = MutableStateFlow(false)
    override val isShowMore: StateFlow<Boolean>
        get() = _isShowMore
    private val _isAppBarExpanded = MutableStateFlow(true)
    override val isAppBarExpanded: StateFlow<Boolean>
        get() = _isAppBarExpanded

    private val capsuleScrollEventChannel = Channel<Unit>(Channel.CONFLATED)
    private val capsuleScrollEventFlow =
        capsuleScrollEventChannel.receiveAsFlow().throttleFirst(1000, TimeUnit.MILLISECONDS)

    private var getCapsuleListJob: Job? = null

    private val _groupInfo = MutableStateFlow(
        GroupProfileData(
            groupName = "Kathrine Turner",
            groupDescription = "추억을 소중하게 여기는 분들을 위한 AR 타임캡슐 앱 ARchive를 소개합니다.\n" +
                    "이 앱으로 뜻깊은 순간들을 영원히 저장하세요.\n" +
                    "이미지, 동영상, 텍스트를 활용해 타임캡슐을 생성하고, AR 카메라로 그 기억들을 다시 열어볼 수 있습니다..\n" +
                    "지정된 시간이나 장소에 도착하면 알림이 울려, 친구들과 함께 개인적인 추억을 나누고 즐길 수 있습니다.\n" +
                    "당신만의 매력적인 이미지로 움직이는 귀여운 캐릭터 타임캡슐 스킨을 직접 제작해보세요.\n" +
                    "우리 앱으로 과거와 현재, 미래를 연결하는 특별한 경험을 즐기실 수 있습니다.",
            groupProfileUrl = "https://www.google.com/#q=quod",
            hasEditPermission = true,
            groupCapsuleNum = "0",
            groupMemberNum = "10",
            groupCreateTime = ""
        )
    )

    override val groupInfo: StateFlow<GroupProfileData>
        get() = _groupInfo

    init {
        viewModelScope.launch {
            capsuleScrollEventFlow.collect {
                if (capsules.value.isEmpty()) {
                    getLatestCapsuleList()
                } else {
                    getCapsuleList()
                }
            }
        }
    }

    override fun setGroupId(groupId: Long) {
        _groupId.value = groupId
        Log.d("아니", "setGroupId, $groupId")
        getGroupDetail()
        getLatestCapsuleList()
    }

    override fun groupDetailEvent(event: GroupDetailViewModel.GroupDetailEvent) {
        viewModelScope.launch {
            _groupDetailEvents.emit(event)
        }
    }

    override fun getGroupDetail() {
        viewModelScope.launch {
            getGroupDetailUseCase(groupId.value).collect { result ->
                result.onSuccess {
                    val groupProfile = it.toGroupProfileData()
                    _groupInfo.emit(groupProfile)
                    _groupMembers.emit(it.members)
                    groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.SwipeRefreshLayoutDismissLoading)
                }.onFail {
                    groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.ShowToastMessage("그룹 정보를 불러오는데 실패했습니다"))
                }
                groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.SwipeRefreshLayoutDismissLoading)
            }

        }
    }

    override fun onCapsuleScrollNearBottom() {
        capsuleScrollEventChannel.trySend(Unit)
    }


    override fun getCapsuleList() {
        if (capsulesHasNextPage.value) {
            getCapsuleListJob?.cancel()
            getCapsuleListJob = viewModelScope.launch {
                capsulesOfGroupPageUseCase(
                    groupId = groupId.value,
                    pagingRequest = IdBasedPagingRequestDto(
                        size = 15,
                        pagingId = capsules.value.lastOrNull()?.capsuleId
                    )
                ).collect{ result ->
                    result.onSuccess {
                        _capsules.value += it.capsules
                        _capsulesHasNextPage.value = it.hasNext
                    }.onFail {
                        groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.ShowToastMessage("그룹의 캡슐 불러오기 실패"))
                    }
                }
            }
        }
    }

    override fun getLatestCapsuleList() {
        getCapsuleListJob?.cancel()
        getCapsuleListJob = viewModelScope.launch {
            capsulesOfGroupPageUseCase(
                groupId = groupId.value,
                pagingRequest = IdBasedPagingRequestDto(
                    size = 15,
                    pagingId = null
                )
            ).collect{ result ->
                result.onSuccess {
                    _capsules.value = it.capsules
                    _capsulesHasNextPage.value = it.hasNext
                }.onFail {
                    groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.ShowToastMessage("그룹의 캡슐 불러오기 실패"))
                }
            }
        }
    }

    override fun setShowMore() {
        _isShowMore.value = !_isShowMore.value
    }

    override fun setIsAppBarExpanded(boolean: Boolean) {
        _isAppBarExpanded.value = boolean
    }

    override fun leaveGroup() {
        groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.ShowLoading)
        viewModelScope.launch {
            leaveGroupUseCase(groupId.value).collect { result ->
                result.onSuccess {
                    groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.LeaveGroupSuccess)
                }.onFail {
                    groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.ShowToastMessage("그룹 탈퇴를 실패했습니다."))
                }
            }
            groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.DismissLoading)
        }
    }

    override fun closureGroup(){
        groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.ShowLoading)
        viewModelScope.launch {
            deleteGroupUseCase(groupId.value).collect{ result ->
                result.onSuccess {
                    groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.SuccessClosureGroup)
                }.onFail {
                    groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.ShowToastMessage("그룹 폐쐐를 실패했습니다."))
                }.onException { it ->
                    it.message?.let { message ->
                        groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.ShowToastMessage(message))
                    }
                }
            }
            groupDetailEvent(GroupDetailViewModel.GroupDetailEvent.DismissLoading)
        }

    }

    override fun updateCapsuleOpenState(capsuleIndex: Int, capsuleId: Long) {
        viewModelScope.launch {
            val newList = _capsules.value
            newList[capsuleIndex].isOpened = true
            _capsules.emit(newList)
        }
    }

}