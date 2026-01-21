package com.pet.android.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.pet.android.databinding.ActivityLoginBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.ui.pet.PetListActivity
import com.pet.android.ui.setting.SettingActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun getViewBinding() = ActivityLoginBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        observeLoginState()
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
            viewModel.login(username, password)
        }
        binding.ivLogo.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeLoginState() {
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
                    navigateToPetList()
                }
                is Resource.Error -> {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToPetList() {
        val intent = Intent(this, PetListActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackButtonPressed() {
        finishAffinity()
    }
}
