package com.droidblossom.archive.presentation.ui.mypage.friend.addfriend

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.droidblossom.archive.R
import com.droidblossom.archive.databinding.FragmentFriendSearchNicknameBinding
import com.droidblossom.archive.presentation.base.BaseFragment
import com.droidblossom.archive.presentation.customview.PermissionDialogFragment
import com.droidblossom.archive.presentation.ui.camera.CameraFragment
import com.droidblossom.archive.presentation.ui.mypage.friend.addfriend.adapter.AddFriendRVA
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFriendNicknameFragment :
    BaseFragment<AddFriendViewModelImpl, FragmentFriendSearchNicknameBinding>(R.layout.fragment_friend_search_nickname) {

    override val viewModel: AddFriendViewModelImpl by viewModels()

    lateinit var navController: NavController

    private val addFriendRVA by lazy {
        AddFriendRVA { position ->
            viewModel.checkAddFriendList(position)
        }
    }

    private val requestContactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                navController.navigate(R.id.action_searchFriendNicknameFragment_to_searchFriendNumberFragment)
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                    showToastMessage("앱에서 친구를 찾기 위해 연락처 접근 권한이 필요합니다. 권한을 허용해 주세요.")
                } else {
                    showSettingsDialog(PermissionDialogFragment.PermissionType.CONTACTS)
                }
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel

        navController = Navigation.findNavController(view)
        initView()
    }

    private fun initView() {

        val layoutParams = binding.closeBtn.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin += getStatusBarHeight()
        binding.closeBtn.layoutParams = layoutParams

        binding.recycleView.adapter = addFriendRVA

        binding.closeBtn.setOnClickListener {
            (activity as AddFriendActivity).finish()
        }

        binding.searchOpenEditT.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                if (!binding.searchOpenEditT.text.isNullOrEmpty()) {
                    viewModel.searchTag()
                }
                true
            }
            false
        }
        binding.searchOpenEditT.addTextChangedListener {
            viewModel.resetList()
        }

        binding.addCV.setOnClickListener {
            requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        binding.searchOpenBtnT.setOnClickListener {
            val imm = requireActivity().getSystemService(InputMethodManager::class.java)
            imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
            viewModel.searchTag()
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addEvent.collect { event ->
                    when (event) {
                        is AddFriendViewModel.AddEvent.ShowToastMessage -> {
                            showToastMessage(event.message)
                        }

                        is AddFriendViewModel.AddEvent.NotificationChange -> {
                            addFriendRVA.notifyDataSetChanged()
                        }

                        else -> {}
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addFriendListUI.collect { friends ->
                    addFriendRVA.submitList(friends)
                }
            }
        }
    }
}