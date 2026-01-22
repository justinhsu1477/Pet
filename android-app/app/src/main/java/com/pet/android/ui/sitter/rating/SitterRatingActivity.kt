package com.pet.android.ui.sitter.rating

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pet.android.R
import com.pet.android.data.model.SitterRatingStatsResponse
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

        setupToolbar(sitterName)
        setupRecyclerView()
        observeStates()

        sitterId?.let { viewModel.load(it) } ?: run {
            showError("找不到保母 ID")
        }
    }

    private fun setupToolbar(sitterName: String) {
        binding.toolbar.title = "$sitterName 的評價"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        binding.rvRatings.layoutManager = LinearLayoutManager(this)
        binding.rvRatings.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun observeStates() {
        viewModel.statsState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    // no-op
                }
                is Resource.Success -> {
                    updateStatsUI(state.data)
                }
                is Resource.Error -> {
                    showError(state.message)
                }
            }
        }

        viewModel.listState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val data = state.data
                    if (data.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvRatings.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvRatings.visibility = View.VISIBLE
                        adapter.submitList(data)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showError(state.message)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateStatsUI(stats: SitterRatingStatsResponse) {
        // Average rating
        binding.tvAverageRating.text = "%.1f".format(stats.average)
        binding.ratingBarAverage.rating = stats.average.toFloat()

        // Total reviews
        binding.tvTotalReviews.text = getString(R.string.reviews_count, stats.count.toInt())

        // Star breakdown
        val total = stats.count.coerceAtLeast(1)

        binding.progressStar5.progress = ((stats.star5 * 100) / total).toInt()
        binding.tvStar5Count.text = stats.star5.toString()

        binding.progressStar4.progress = ((stats.star4 * 100) / total).toInt()
        binding.tvStar4Count.text = stats.star4.toString()

        binding.progressStar3.progress = ((stats.star3 * 100) / total).toInt()
        binding.tvStar3Count.text = stats.star3.toString()

        binding.progressStar2.progress = ((stats.star2 * 100) / total).toInt()
        binding.tvStar2Count.text = stats.star2.toString()

        binding.progressStar1.progress = ((stats.star1 * 100) / total).toInt()
        binding.tvStar1Count.text = stats.star1.toString()
    }

    private fun showError(message: String) {
        binding.tvTotalReviews.text = message
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
