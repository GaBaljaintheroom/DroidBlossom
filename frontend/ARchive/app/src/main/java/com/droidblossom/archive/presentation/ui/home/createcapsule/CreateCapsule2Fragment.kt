package com.droidblossom.archive.presentation.ui.home.createcapsule

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.droidblossom.archive.R
import com.droidblossom.archive.databinding.FragmentCreateCapsule2Binding
import com.droidblossom.archive.presentation.base.BaseFragment
import com.droidblossom.archive.presentation.ui.home.createcapsule.adapter.SkinRVA
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CreateCapsule2Fragment :
    BaseFragment<CreateCapsuleViewModelImpl, FragmentCreateCapsule2Binding>(R.layout.fragment_create_capsule2) {

    override val viewModel: CreateCapsuleViewModelImpl by activityViewModels()
    lateinit var navController: NavController
    private lateinit var callback: OnBackPressedCallback

    private val skinRVA by lazy {
        SkinRVA { viewModel.changeSkin(it) }
    }

    //그룹캡슐이 아닐 경우 바로 엑티비티 닫기
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.groupTypeInt != 1) {
                    requireActivity().finish()
                } else {
                    super.remove()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        navController = Navigation.findNavController(view)
        binding.recycleView.adapter = skinRVA
        binding.recycleView.itemAnimator = null

        initSearchEdit()

        //테스트용
        Toast.makeText(requireContext(), viewModel.capsuleType.value.title, Toast.LENGTH_SHORT)
            .show()
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.create2Events.collect {
                    when (it) {
                        CreateCapsuleViewModel.Create2Event.NavigateTo3 -> {
                            navController.navigate(R.id.action_createCapsule2Fragment_to_createCapsule3Fragment)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.skins.collect {
                    skinRVA.submitList(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSearchOpen.collect {
                    if (it){
                        binding.searchOpenEditT.requestFocus()
                        val imm = requireActivity().getSystemService(InputMethodManager::class.java)
                        imm.showSoftInput(binding.searchOpenEditT, InputMethodManager.SHOW_IMPLICIT);

                    }
                }
            }
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    fun initSearchEdit(){
        binding.searchOpenEditT.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                if (!binding.searchOpenEditT.text.isNullOrEmpty()) {
                    viewModel.searchSkin()
                }
                true
            }
            false
        }
        binding.searchOpenEditT.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.closeSearchSkin()
            }
        }
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val focusedView = binding.searchOpenBtn
                val outRect = Rect()
                focusedView.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    focusedView.clearFocus()
                    val imm = requireActivity().getSystemService(InputMethodManager::class.java)
                    imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
                }
            }
            false
        }

        binding.recycleView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val focusedView = binding.searchOpenBtn
                val outRect = Rect()
                focusedView.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    focusedView.clearFocus()
                    val imm = requireActivity().getSystemService(InputMethodManager::class.java)
                    imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
                }
            }
            false
        }

        binding.searchOpenBtnT.setOnClickListener {
            val imm = requireActivity().getSystemService(InputMethodManager::class.java)
            imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        }
    }
}