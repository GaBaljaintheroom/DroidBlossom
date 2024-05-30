package com.droidblossom.archive.presentation.ui.mypage.friend.detail.group.management.page

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.droidblossom.archive.R
import com.droidblossom.archive.databinding.FragmentManagementInvitableFriendsBinding
import com.droidblossom.archive.presentation.base.BaseFragment
import com.droidblossom.archive.presentation.ui.mypage.friend.detail.group.management.ManagementGroupMemberViewModelImpl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManagementInvitableFriendsFragment :
    BaseFragment<ManagementGroupMemberViewModelImpl, FragmentManagementInvitableFriendsBinding>(
        R.layout.fragment_management_invitable_friends
    ) {
    override val viewModel: ManagementGroupMemberViewModelImpl by activityViewModels()


    override fun observeData() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel

        initRV()
        initView()
    }

    private fun initView() {

        with(binding) {



        }
    }

    private fun initRV() {

    }

}