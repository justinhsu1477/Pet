package com.pet.android.ui.sitter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.pet.android.R
import com.pet.android.databinding.ActivitySitterMainBinding
import com.pet.android.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Sitter 主頁面容器
 * 使用 Bottom Navigation + Navigation Component 實現三個主要功能模組的切換
 */
@AndroidEntryPoint
class SitterMainActivity : BaseActivity<ActivitySitterMainBinding>() {

    override fun getViewBinding() = ActivitySitterMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 將 BottomNavigationView 與 NavController 連接
        binding.bottomNavigation.setupWithNavController(navController)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SitterMainActivity::class.java))
        }
    }
}
