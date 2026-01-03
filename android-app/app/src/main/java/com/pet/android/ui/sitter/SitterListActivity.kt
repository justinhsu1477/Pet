package com.pet.android.ui.sitter

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pet.android.databinding.ActivitySitterListBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SitterListActivity : BaseActivity<ActivitySitterListBinding>() {

    private val viewModel: SitterViewModel by viewModels()
    private lateinit var sitterAdapter: SitterAdapter

    override fun getViewBinding() = ActivitySitterListBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRecyclerView()
        observeSittersState()
        viewModel.loadSitters()
    }

    private fun setupRecyclerView() {
        sitterAdapter = SitterAdapter { sitter ->
            Toast.makeText(this, "點擊: ${sitter.name}", Toast.LENGTH_SHORT).show()
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
