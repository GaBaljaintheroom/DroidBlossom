package com.droidblossom.archive.presentation.ui.home.createcapsule

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.droidblossom.archive.domain.model.common.AddressData
import com.droidblossom.archive.domain.model.common.Dummy
import com.droidblossom.archive.domain.model.common.FileName
import com.droidblossom.archive.domain.model.common.Location
import com.droidblossom.archive.domain.model.common.Skin
import com.droidblossom.archive.domain.model.s3.S3UrlRequest
import com.droidblossom.archive.domain.model.secret.SecretCapsuleCreateRequest
import com.droidblossom.archive.domain.usecase.capsule.GetAddressUseCase
import com.droidblossom.archive.domain.usecase.kakao.ToAddressUseCase
import com.droidblossom.archive.domain.usecase.s3.S3UrlsGetUseCase
import com.droidblossom.archive.domain.usecase.secret.SecretCapsuleCreateUseCase
import com.droidblossom.archive.presentation.base.BaseViewModel
import com.droidblossom.archive.util.DateUtils
import com.droidblossom.archive.util.FileUtils
import com.droidblossom.archive.util.S3Util
import com.droidblossom.archive.util.onFail
import com.droidblossom.archive.util.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreateCapsuleViewModelImpl @Inject constructor(
    private val getAddressUseCase: GetAddressUseCase,
    private val secretCapsuleCreateUseCase: SecretCapsuleCreateUseCase,
    private val toAddressUseCase: ToAddressUseCase,
    private val s3UrlsGetUseCase : S3UrlsGetUseCase,
    private val s3Util: S3Util,
) : BaseViewModel(), CreateCapsuleViewModel {

    companion object{
        const val S3DIRECTORY = "capsuleContents"
        const val IMAGEEXTENSION = "image/jpeg"
    }

    override var groupTypeInt: Int = 1
    private val _capsuleTypeCreateIs = MutableStateFlow(CreateCapsuleViewModel.CapsuleTypeCreate.SECRET)
    override val capsuleTypeCreateIs: StateFlow<CreateCapsuleViewModel.CapsuleTypeCreate>
        get() = _capsuleTypeCreateIs

    //create1

    private val _create1Events = MutableSharedFlow<CreateCapsuleViewModel.Create1Event>()
    override val create1Events: SharedFlow<CreateCapsuleViewModel.Create1Event>
        get() = _create1Events.asSharedFlow()
    private val _groupId = MutableStateFlow(0)
    override val groupId: StateFlow<Int>
        get() = _groupId

    //create2
    private val _create2Events = MutableSharedFlow<CreateCapsuleViewModel.Create2Event>()
    override val create2Events: SharedFlow<CreateCapsuleViewModel.Create2Event>
        get() = _create2Events
    private val _skinId = MutableStateFlow<Long>(0)
    override val skinId: StateFlow<Long>
        get() = _skinId
    private val _skins = MutableStateFlow(
        listOf(Skin(1, "", false), Skin(2, "", false), Skin(3, "", false))
    )
    override val skins: StateFlow<List<Skin>>
        get() = _skins

    private val _isSearchOpen = MutableStateFlow(false)
    override val isSearchOpen: StateFlow<Boolean>
        get() = _isSearchOpen


    //create3
    private val _create3Events = MutableSharedFlow<CreateCapsuleViewModel.Create3Event>()
    override val create3Events: SharedFlow<CreateCapsuleViewModel.Create3Event>
        get() = _create3Events.asSharedFlow()
    override val capsuleTitle: MutableStateFlow<String> = MutableStateFlow("")

    override val capsuleContent: MutableStateFlow<String> = MutableStateFlow("")

    private val _capsuleLocationName = MutableStateFlow("")
    override val capsuleLocationName: StateFlow<String>
        get() = _capsuleLocationName

    private val _capsuleDueDate = MutableStateFlow("")
    override val capsuleDueDate: StateFlow<String>
        get() = _capsuleDueDate

    private val _capsuleLocation = MutableStateFlow(Location(0.0, 0.0))
    override val capsuleLocation: StateFlow<Location>
        get() = _capsuleLocation

    private val _capsuleImg = MutableStateFlow(listOf<FileName>())
    override val capsuleImg: StateFlow<List<FileName>>
        get() = _capsuleImg

    private val _capsuleImgUrls = MutableStateFlow(listOf<String>())
    override val capsuleImgUrls: StateFlow<List<String>>
        get() = _capsuleImgUrls
    private val _isSelectTimeCapsule = MutableStateFlow(true)
    override val isSelectTimeCapsule: StateFlow<Boolean>
        get() = _isSelectTimeCapsule

    private val _imgUris = MutableStateFlow(listOf<Dummy>(Dummy(null, true)))
    override val imgUris: StateFlow<List<Dummy>>
        get() = _imgUris

    private val _files = MutableStateFlow<List<File>>(emptyList())
    override val files: StateFlow<List<File>> = _files

    //dateDialog
    override val year = MutableStateFlow<Int>(DateUtils.getCurrentYear())
    override val month: MutableStateFlow<Int> = MutableStateFlow<Int>(DateUtils.getCurrentMonth())
    override val day: MutableStateFlow<Int> = MutableStateFlow<Int>(DateUtils.getCurrentDay())
    override val hour: MutableStateFlow<Int> = MutableStateFlow<Int>(DateUtils.getCurrentHour())
    override val min: MutableStateFlow<Int> = MutableStateFlow<Int>(DateUtils.getCurrentMin())
    private val _isSelectTime = MutableStateFlow<Boolean>(false)
    override val isSelectTime: StateFlow<Boolean>
        get() = _isSelectTime

    private val _capsuleLongitude = MutableStateFlow(0.0)
    override val capsuleLongitude: StateFlow<Double>
        get() = _capsuleLongitude

    private val _capsuleLatitude =MutableStateFlow(0.0)
    override val capsuleLatitude: StateFlow<Double>
        get() = _capsuleLatitude
    private val _dueTime = MutableStateFlow("")
    override val dueTime: StateFlow<String>
        get() = _dueTime
    private val _address = MutableStateFlow(AddressData())
    override val address: StateFlow<AddressData>
        get() = _address

    private var fileMetaDataLists = listOf<FileName>()
    //create1
    override fun move1To2() {
        viewModelScope.launch {
            _create1Events.emit(CreateCapsuleViewModel.Create1Event.NavigateTo2)
        }
    }

    override fun choseCapsuleType(type: Int) {
        viewModelScope.launch {
            when (groupTypeInt) {
                1 -> {
                    _capsuleTypeCreateIs.emit(CreateCapsuleViewModel.CapsuleTypeCreate.GROUP)
                }

                2 -> {
                    _capsuleTypeCreateIs.emit(CreateCapsuleViewModel.CapsuleTypeCreate.PUBLIC)
                }

                3 -> {
                    _capsuleTypeCreateIs.emit(CreateCapsuleViewModel.CapsuleTypeCreate.SECRET)
                }

                else -> {}
            }
        }
    }


    //create2
    override fun move2To3() {
        viewModelScope.launch {
            _skinId.emit(_skins.value.find { it.isClicked }?.skinId?.toLong() ?: 0.toLong())
        }
        viewModelScope.launch {
            _create2Events.emit(CreateCapsuleViewModel.Create2Event.NavigateTo3)
        }
    }

    override fun openSearchSkin() {
        viewModelScope.launch {
            _isSearchOpen.emit(true)
        }
    }

    override fun closeSearchSkin() {
        viewModelScope.launch {
            _isSearchOpen.emit(false)
        }
    }

    override fun searchSkin() {
        viewModelScope.launch {
            //스킨 검색 API
        }
    }

    override fun changeSkin(skin: Skin) {
        val submitList = skins.value
        submitList.map { it.isClicked = false }
        submitList[submitList.indexOf(skin)].isClicked = true
        viewModelScope.launch {
            _skins.emit(submitList)
        }
    }


    //creat3
    /**/
    override fun moveFinish() {
        Log.d("캡슐생성","${capsuleTypeCreateIs.value.title},${address.value},${dueTime.value}")
        viewModelScope.launch {
            when(capsuleTypeCreateIs.value){
                CreateCapsuleViewModel.CapsuleTypeCreate.PUBLIC ->{

                }
                CreateCapsuleViewModel.CapsuleTypeCreate.SECRET -> {
                    Log.d("캡슐생성","${capsuleTypeCreateIs.value.title},${address.value},${dueTime.value}")
                    secretCapsuleCreateUseCase(
                        SecretCapsuleCreateRequest(
                        capsuleSkinId = 1,
                        capsuleType = capsuleTypeCreateIs.value.title,
                        content = capsuleContent.value ,
                        directory = "string",
                        dueDate = dueTime.value,
                        fileNames = listOf(FileName("string","string")),
                        addressData = address.value,
                        latitude = capsuleLatitude.value,
                        longitude = capsuleLongitude.value ,
                        title = capsuleTitle.value,
                     )
                    ).collect{ result ->
                        Log.d("캡슐생성","${result}")
                    }
                }
                CreateCapsuleViewModel.CapsuleTypeCreate.GROUP -> {

                }
            }
            _create3Events.emit(CreateCapsuleViewModel.Create3Event.ClickFinish)
        }
    }

    fun moveFinishh() {
        if (files.value.isEmpty()){
            secretCapsuleCreate()
        }else{
            fileMetaDataLists = files.value.map { file ->
                FileName(
                    extension = IMAGEEXTENSION,
                    fileName = file.name
                )
            }
            val getS3UrlData = S3UrlRequest(S3DIRECTORY, fileMetaDataLists)
            getUploadUrls(getS3UrlData)
        }
    }

    override fun makeFiles(files : List<File>){
        _files.value = files
    }
    override fun moveLocation() {
        viewModelScope.launch {
            _create3Events.emit(CreateCapsuleViewModel.Create3Event.ClickLocation)
        }
    }

    override fun coordToAddress(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _capsuleLatitude.value = latitude
            _capsuleLongitude.value = longitude
            getAddressUseCase(latitude, longitude).collect{result ->
                result.onSuccess {
                    _capsuleLocationName.emit(it.fullRoadAddressName)
                    _address.emit(it)
                }.onFail {
                    _capsuleLocationName.emit("에러")
                }
            }
        }
    }
    override fun getDueTime(time: String) {
        viewModelScope.launch {
            _dueTime.emit(time)
        }
    }

    override fun moveDate() {
        viewModelScope.launch {
            _create3Events.emit(CreateCapsuleViewModel.Create3Event.ClickDate)
        }
    }

    override fun goSelectTime() {
        viewModelScope.launch {
            _isSelectTime.emit(true)
        }
    }

    override fun moveImgUpLoad() {
        viewModelScope.launch {
            _create3Events.emit(CreateCapsuleViewModel.Create3Event.ClickImgUpLoad)
        }
    }

    override fun moveSingleImgUpLoad() {
        viewModelScope.launch {
            _create3Events.emit(CreateCapsuleViewModel.Create3Event.CLickSingleImgUpLoad)
        }
    }

    override fun selectTimeCapsule() {
        viewModelScope.launch { _isSelectTimeCapsule.emit(true) }
    }

    override fun selectCapsule() {
        viewModelScope.launch { _isSelectTimeCapsule.emit(false) }
    }

    override fun addImgUris(list: List<Dummy>) {
        val submitList = list + imgUris.value
        if (submitList.size > 5) {
            val listSize5 = submitList.slice(0..4)
            viewModelScope.launch { _imgUris.emit(listSize5) }
        } else {
            viewModelScope.launch { _imgUris.emit(submitList) }
        }
    }

    override fun submitUris(list: List<Dummy>) {
        val submitList = if (list.none { it.last }) {
            list + listOf(Dummy(null, true))
        } else list
        viewModelScope.launch { _imgUris.emit(submitList) }
    }

    override fun getUploadUrl(getS3UrlData : S3UrlRequest, file : File){
        viewModelScope.launch {
            s3UrlsGetUseCase(getS3UrlData.toDto()).collect{result ->
                result.onSuccess {
                    uploadFileToS3(file,it.preSignedUrls[0])
                }.onFail {
                    Log.d("getUploadUrl","getUploadUrl 실패")
                }
            }
        }
    }

    private fun uploadFileToS3(file: File, signedUrl: String){
        viewModelScope.launch(Dispatchers.IO){
            try {
                s3Util.uploadImageWithPresignedUrl(file,signedUrl)
            }catch (e:Exception){
                Log.d("uploadFileToS3", "uploadFileToS3 : ${e.message}")

            }
        }
    }

    override fun getUploadUrls(getS3UrlData : S3UrlRequest){
        viewModelScope.launch {
            s3UrlsGetUseCase(getS3UrlData.toDto()).collect{result ->
                result.onSuccess {
                    uploadFilesToS3(files.value,it.preSignedUrls)
                }.onFail {
                    Log.d("getUploadUrls","getUploadUrl 실패")
                }
            }
        }
    }

    private fun uploadFilesToS3(files: List<File>, signedUrls: List<String>) {
        viewModelScope.launch {
            val uploadJobs = files.zip(signedUrls).map { (file, url) ->
                launch(Dispatchers.IO) {
                    try {
                        s3Util.uploadImageWithPresignedUrl(file, url)
                        Log.d("uploadFilesToS3", "File ${file.name} uploaded successfully")
                    } catch (e: Exception) {
                        Log.e("uploadFilesToS3", "Failed to upload ${file.name}: ${e.message}")
                    }
                }
            }

            uploadJobs.joinAll()
            Log.d("uploadFilesToS3", "All files uploaded successfully")
            secretCapsuleCreate()
        }
    }

    private fun secretCapsuleCreate(){
        viewModelScope.launch{
            when(capsuleTypeCreateIs.value){
                CreateCapsuleViewModel.CapsuleTypeCreate.SECRET -> {
                    secretCapsuleCreateUseCase(
                        SecretCapsuleCreateRequest(
                            capsuleSkinId = 1,
                            capsuleType = capsuleTypeCreateIs.value.title,
                            content = capsuleContent.value ,
                            directory = fileMetaDataLists.takeUnless { it.isEmpty() }?.let { S3DIRECTORY } ?: "",
                            dueDate = dueTime.value,
                            fileNames = fileMetaDataLists,
                            addressData = address.value,
                            latitude = capsuleLatitude.value,
                            longitude = capsuleLongitude.value ,
                            title = capsuleTitle.value,
                        )
                    ).collect{ result ->
                        Log.d("캡슐생성","${result}")
                    }
                }
                CreateCapsuleViewModel.CapsuleTypeCreate.GROUP -> {
                    Log.d("캡슐 타입", " 그룹 : ${capsuleTypeCreateIs.value}")

                }
                CreateCapsuleViewModel.CapsuleTypeCreate.PUBLIC -> {
                    Log.d("캡슐 타입", "공개 : ${capsuleTypeCreateIs.value}")
                }
            }
        }
    }

}