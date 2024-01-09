package com.droidblossom.archive.presentation.ui.home.createcapsule

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.droidblossom.archive.R
import com.droidblossom.archive.databinding.FragmentCreateCapsule2Binding
import com.droidblossom.archive.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateCapsule2Fragment :
    BaseFragment<CreateCapsuleViewModelImpl, FragmentCreateCapsule2Binding>(R.layout.fragment_create_capsule2) {

    override val viewModel: CreateCapsuleViewModelImpl by activityViewModels()

    override fun observeData() {

    }

}