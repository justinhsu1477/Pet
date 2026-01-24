package com.pet.android.ui.sitter.statistics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.databinding.ActivitySitterStatisticsBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SitterStatisticsActivity : BaseActivity<ActivitySitterStatisticsBinding>() {

    private val viewModel: SitterStatisticsViewModel by viewModels()

    @Inject
    lateinit var userPreferences: UserPreferencesManager

    private val dailyRevenueAdapter = DailyRevenueAdapter()
    private val simpleRatingAdapter = SimpleRatingAdapter()

    private var sitterId: String? = null

    override fun getViewBinding() = ActivitySitterStatisticsBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupRecyclerViews()
        loadSitterId()
        observeStatistics()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerViews() {
        // 每日收入趨勢列表
        binding.rvDailyTrend.apply {
            layoutManager = LinearLayoutManager(this@SitterStatisticsActivity)
            adapter = dailyRevenueAdapter
        }

        // 最新評價列表
        binding.rvLatestRatings.apply {
            layoutManager = LinearLayoutManager(this@SitterStatisticsActivity)
            adapter = simpleRatingAdapter
        }
    }

    private fun loadSitterId() {
        lifecycleScope.launch {
            sitterId = userPreferences.userId.first()
            val username = userPreferences.username.first()
            val role = userPreferences.userRole.first()

            Log.d(TAG, "loadSitterId - userId: $sitterId, username: $username, role: $role")

            if (sitterId != null) {
                Log.d(TAG, "Loading statistics for sitterId: $sitterId")
                viewModel.loadStatistics(sitterId!!)
            } else {
                Log.e(TAG, "userId is null! Cannot load statistics. Username: $username, Role: $role")
                Toast.makeText(this@SitterStatisticsActivity, "無法取得保母 ID，請重新登入", Toast.LENGTH_LONG).show()
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun observeStatistics() {
        viewModel.statisticsState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let { statistics ->
                        displayStatistics(statistics)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayStatistics(statistics: com.pet.android.data.model.BookingStatisticsResponse) {
        val numberFormat = NumberFormat.getNumberInstance(Locale.TAIWAN)

        // 預約統計
        val bookingStats = statistics.bookingStats.currentMonth
        binding.tvTotalBookings.text = bookingStats.total.toString()
        binding.tvPendingBookings.text = bookingStats.pending.toString()
        binding.tvCompletedBookings.text = bookingStats.completed.toString()
        binding.tvRejectedBookings.text = bookingStats.rejectedOrCancelled.toString()

        // 收入統計
        val revenueStats = statistics.revenueStats
        binding.tvMonthlyRevenue.text = "NT$ ${numberFormat.format(revenueStats.monthlyRevenue.toInt())}"
        binding.tvWeeklyRevenue.text = "NT$ ${numberFormat.format(revenueStats.weeklyRevenue.toInt())}"

        // 每日收入趨勢
        dailyRevenueAdapter.submitList(revenueStats.dailyTrend)

        // 評價統計
        val ratingStats = statistics.ratingStats
        binding.tvAverageRating.text = String.format("%.1f", ratingStats.averageRating)
        binding.tvFiveStarPercentage.text = String.format("%.0f%%", ratingStats.fiveStarPercentage)
        binding.tvTotalRatings.text = ratingStats.totalRatings.toString()

        // 最新評價
        simpleRatingAdapter.submitList(ratingStats.latestRatings)
    }

    companion object {
        private const val TAG = "SitterStatisticsActivity"

        fun start(context: Context) {
            context.startActivity(Intent(context, SitterStatisticsActivity::class.java))
        }
    }
}
