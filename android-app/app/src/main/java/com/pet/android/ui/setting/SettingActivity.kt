package com.pet.android.ui.setting

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pet.android.R
import com.pet.android.data.preferences.EnvironmentManager.Environment
import com.pet.android.databinding.ActivitySettingBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    private val viewModel: SettingViewModel by viewModels()

    override fun getViewBinding() = ActivitySettingBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        setupToolbarNavigation(binding.toolbar)
        observeViewModel()
    }

    private fun setupViews() {
        // Version Info
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            binding.tvVersion.text = "v$version"
        } catch (e: Exception) {
            binding.tvVersion.text = "Unknown"
        }

        // Environment Switching
        binding.rgEnvironment.setOnCheckedChangeListener { _, checkedId ->
            val environment = when (checkedId) {
                R.id.rbDev -> Environment.DEVELOPMENT
                R.id.rbStage -> Environment.STAGING
                R.id.rbProd -> Environment.PRODUCTION
                else -> return@setOnCheckedChangeListener
            }
            if (viewModel.currentEnvironment.value != environment) {
                 viewModel.setEnvironment(environment)
                 Toast.makeText(this, "環境已切換至 ${environment.displayName}，請重新啟動應用程式以生效系統層級變更", Toast.LENGTH_LONG).show()
            }
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            navigateToLogin()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentEnvironment.collect { env ->
                    val rbId = when (env) {
                        Environment.DEVELOPMENT -> R.id.rbDev
                        Environment.STAGING -> R.id.rbStage
                        Environment.PRODUCTION -> R.id.rbProd
                    }
                    if (binding.rgEnvironment.checkedRadioButtonId != rbId) {
                        binding.rgEnvironment.check(rbId)
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        // Clear back stack so user can't go back to Settings or Main screen
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}