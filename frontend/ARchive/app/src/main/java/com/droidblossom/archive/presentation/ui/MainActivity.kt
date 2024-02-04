package com.droidblossom.archive.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.droidblossom.archive.BuildConfig
import com.droidblossom.archive.R
import com.droidblossom.archive.databinding.ActivityMainBinding
import com.droidblossom.archive.presentation.base.BaseActivity
import com.droidblossom.archive.presentation.ui.camera.CameraFragment
import com.droidblossom.archive.presentation.ui.home.HomeFragment
import com.droidblossom.archive.presentation.ui.mypage.MyPageFragment
import com.droidblossom.archive.presentation.ui.skin.SkinFragment
import com.droidblossom.archive.presentation.ui.social.SocialFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<Nothing?, ActivityMainBinding>(R.layout.activity_main) {

    override val viewModel: Nothing? = null
    lateinit var viewBinding: ActivityMainBinding

    override fun observeData() {}

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        viewBinding = binding
        showFragment(HomeFragment.newIntent(), HomeFragment.TAG)


        binding.fab.setOnClickListener {
            showFragment(CameraFragment.newIntent(), CameraFragment.TAG)
            binding.bottomNavigation.selectedItemId = R.id.menuCamera
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menuHome -> {
                    showFragment(HomeFragment.newIntent(), HomeFragment.TAG)
                    return@setOnItemSelectedListener true
                }

                R.id.menuSkin -> {
                    showFragment(SkinFragment.newIntent(), SkinFragment.TAG)
                    return@setOnItemSelectedListener true
                }

                R.id.menuSocial -> {
                    showFragment(SocialFragment(), SocialFragment.TAG)
                    return@setOnItemSelectedListener true
                }

                R.id.menuMyPage -> {
                    showFragment(MyPageFragment.newIntent(), MyPageFragment.TAG)
                    return@setOnItemSelectedListener true
                }

                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }

    }
    private fun showFragment(fragment: Fragment, tag: String) {
        val findFragment = supportFragmentManager.findFragmentByTag(tag)
        supportFragmentManager.fragments.forEach {
            supportFragmentManager.beginTransaction().hide(it).commitAllowingStateLoss()
        }
        findFragment?.let {
            supportFragmentManager.beginTransaction().show(it).commitAllowingStateLoss()
        } ?: kotlin.run {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainNavHost, fragment, tag)
                .commitAllowingStateLoss()
        }
    }

    companion object{
        fun goMain(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

}