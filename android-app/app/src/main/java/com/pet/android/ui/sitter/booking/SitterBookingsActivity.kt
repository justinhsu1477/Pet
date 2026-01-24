package com.pet.android.ui.sitter.booking

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.pet.android.R
import com.pet.android.data.model.BookingStatus
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.databinding.ActivitySitterBookingsBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.ui.sitter.statistics.SitterStatisticsActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class SitterBookingsActivity : BaseActivity<ActivitySitterBookingsBinding>() {

    private val viewModel: SitterBookingsViewModel by viewModels()

    @Inject
    lateinit var userPreferences: UserPreferencesManager

    private lateinit var bookingAdapter: SitterBookingAdapter
    private var sitterId: String? = null

    override fun getViewBinding() = ActivitySitterBookingsBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupFilters()
        loadSitterId()
        observeBookings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sitter_bookings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_statistics -> {
                SitterStatisticsActivity.start(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        bookingAdapter = SitterBookingAdapter { booking ->
            // 點擊預約項目，前往詳情頁
            SitterBookingDetailActivity.start(this, booking.id)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SitterBookingsActivity)
            adapter = bookingAdapter
        }
    }

    private fun setupFilters() {
        // 日期篩選按鈕
        binding.btnFilterDate.setOnClickListener {
            showDatePicker()
        }

        // 狀態篩選 Chips
        binding.chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                viewModel.selectedStatus = null
            } else {
                val checkedChip = group.findViewById<Chip>(checkedIds[0])
                viewModel.selectedStatus = when (checkedChip.id) {
                    R.id.chipPending -> BookingStatus.PENDING
                    R.id.chipConfirmed -> BookingStatus.CONFIRMED
                    R.id.chipCompleted -> BookingStatus.COMPLETED
                    else -> null
                }
            }
            viewModel.applyFilters()
        }

        // 清除篩選按鈕
        binding.btnClearFilters.setOnClickListener {
            binding.chipGroupStatus.clearCheck()
            viewModel.clearFilters()
            binding.tvSelectedDate.text = "選擇日期"
        }

        // 重新整理按鈕
        binding.btnRefresh.setOnClickListener {
            sitterId?.let { viewModel.loadSitterBookings(it) }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            viewModel.selectedDate = date
            binding.tvSelectedDate.text = date.toString()
            viewModel.applyFilters()
        }, year, month, day).show()
    }

    private fun loadSitterId() {
        // 從 UserPreferences 取得 sitterId
        // 假設登入時有存 userId，且 userId 就是 sitterId (保母登入)
        sitterId = runBlocking { userPreferences.userId.first() }

        if (sitterId != null) {
            viewModel.loadSitterBookings(sitterId!!)
        } else {
            Toast.makeText(this, "無法取得保母 ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeBookings() {
        viewModel.bookingsState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE

                    val bookings = resource.data
                    if (bookings.isEmpty()) {
                        binding.recyclerView.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        bookingAdapter.submitList(bookings)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = resource.message
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val TAG = "SitterBookingsActivity"

        fun start(context: Context) {
            context.startActivity(Intent(context, SitterBookingsActivity::class.java))
        }
    }
}
