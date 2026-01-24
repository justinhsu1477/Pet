package com.pet.android.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.pet.android.data.model.UserRole
import com.pet.android.databinding.ActivityLoginBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.ui.booking.BookingHomeActivity
import com.pet.android.ui.pet.PetListActivity
import com.pet.android.ui.setting.SettingActivity
import com.pet.android.ui.sitter.SitterMainActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    companion object {
        private const val TAG = "LoginActivity"
    }

    private val viewModel: LoginViewModel by viewModels()

    override fun getViewBinding() = ActivityLoginBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 檢查是否從登出跳轉而來
        if (intent.getBooleanExtra("SHOW_LOGOUT_MESSAGE", false)) {
            Toast.makeText(this, "您已登出，請重新登入", Toast.LENGTH_SHORT).show()
        }

        // 檢查是否已有有效的 Token，自動登入
        checkAutoLogin()

        setupViews()
        observeLoginState()
    }

    /**
     * 檢查自動登入
     * 如果有有效的 Token，直接跳轉到主頁
     */
    private fun checkAutoLogin() {
        if (viewModel.isLoggedIn()) {
            Log.d(TAG, "Valid token found, auto-login")
            Toast.makeText(this, "自動登入中...", Toast.LENGTH_SHORT).show()
            navigateToHome()
        } else {
            Log.d(TAG, "No valid token, showing login screen")
        }
    }

    private fun setupViews() {
        // Observe and populate saved username
        lifecycleScope.launch {
            viewModel.savedUsername.collectLatest { savedUsername ->
                savedUsername?.let {
                    binding.etUsername.setText(it)
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            // 使用 JWT 登入
            viewModel.jwtLogin(username, password)
        }
        binding.ivLogo.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeLoginState() {
        // 觀察 JWT 登入狀態
        viewModel.jwtLoginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.loadingOverlay.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is Resource.Success -> {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "登入成功", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
                is Resource.Error -> {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 保留舊版登入狀態觀察（向後兼容）
        viewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.loadingOverlay.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is Resource.Success -> {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "登入成功", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
                is Resource.Error -> {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToHome() {
        lifecycleScope.launch {
            val role = viewModel.getUserRoleEnum()
            when (role) {
                UserRole.ADMIN -> {
                    val intent = Intent(this@LoginActivity, PetListActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                UserRole.CUSTOMER -> {
                    // USER 角色導航到預約頁面
                    BookingHomeActivity.start(this@LoginActivity)
                    finish()
                }
                UserRole.SITTER -> {
                    // SITTER 角色導航到主頁面 (包含預約管理、評價、統計三個功能)
                    SitterMainActivity.start(this@LoginActivity)
                    finish()
                }
                else -> {
                    Toast.makeText(this@LoginActivity, "未知角色", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBackButtonPressed() {
        finishAffinity()
    }
}
