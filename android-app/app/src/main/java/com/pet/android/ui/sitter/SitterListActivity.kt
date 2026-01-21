package com.pet.android.ui.sitter

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pet.android.databinding.ActivitySitterListBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SitterListActivity : BaseActivity<ActivitySitterListBinding>() {
    companion object {
        private const val TAG = "SitterListActivity"
    }

    private val viewModel: SitterViewModel by viewModels()
    private lateinit var sitterAdapter: SitterAdapter

    override fun getViewBinding() = ActivitySitterListBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbarNavigation(binding.toolbar)
        setupRecyclerView()
        observeSittersState()
        viewModel.loadSitters()

    }

    private fun setupRecyclerView() {
        sitterAdapter = SitterAdapter { sitter ->

            lifecycleScope.launch {
                val id = sitter.id ?: return@launch
                if (!viewModel.isAdmin()) {
                    Log.d(TAG, "非管理員權限")
                    return@launch
                }
                Log.d(TAG, "管理員")
                com.pet.android.ui.sitter.rating.SitterRatingActivity.start(this@SitterListActivity, id, sitter.name)
            }

        }
        binding.rvSitters.apply {
            layoutManager = LinearLayoutManager(this@SitterListActivity)
            adapter = sitterAdapter
        }
    }

    private fun observeSittersState() {
        viewModel.sittersState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvSitters.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvSitters.visibility = View.VISIBLE
                    sitterAdapter.submitList(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
