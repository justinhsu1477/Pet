package com.pet.android.ui.sitter.rating

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pet.android.databinding.ActivitySitterRatingBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SitterRatingActivity : BaseActivity<ActivitySitterRatingBinding>() {

    private val viewModel: SitterRatingViewModel by viewModels()
    private val adapter = SitterRatingAdapter()

    override fun getViewBinding(): ActivitySitterRatingBinding =
        ActivitySitterRatingBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sitterId = intent.getStringExtra(EXTRA_SITTER_ID)
        val sitterName = intent.getStringExtra(EXTRA_SITTER_NAME) ?: "保母"

        setupToolbar(binding.toolbar, title = "$sitterName 的評價")

        binding.rvRatings.layoutManager = LinearLayoutManager(this)
        binding.rvRatings.adapter = adapter

        observeStates()

        sitterId?.let { viewModel.load(it) } ?: run {
            binding.tvStats.text = "找不到保母 ID"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeStates() {
        viewModel.statsState.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // no-op
                }
                is Resource.Success -> {
                    val s = it.data
                    binding.tvStats.text = "平均 ${"%.1f".format(s.average)}（${s.count} 筆）"
                }
                is Resource.Error -> {
                    binding.tvStats.text = it.message
                }
            }
        }

        viewModel.listState.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(it.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvStats.text = it.message
                }
            }
        }
    }

    companion object {
        private const val EXTRA_SITTER_ID = "extra_sitter_id"
        private const val EXTRA_SITTER_NAME = "extra_sitter_name"

        fun start(context: Context, sitterId: String, sitterName: String) {
            val i = Intent(context, SitterRatingActivity::class.java)
            i.putExtra(EXTRA_SITTER_ID, sitterId)
            i.putExtra(EXTRA_SITTER_NAME, sitterName)
            context.startActivity(i)
        }
    }
}