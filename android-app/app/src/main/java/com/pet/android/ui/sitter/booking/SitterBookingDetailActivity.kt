package com.pet.android.ui.sitter.booking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.pet.android.data.model.BookingStatus
import com.pet.android.data.model.SitterBookingResponse
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.databinding.ActivitySitterBookingDetailBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SitterBookingDetailActivity : BaseActivity<ActivitySitterBookingDetailBinding>() {

    private val viewModel: SitterBookingDetailViewModel by viewModels()

    @Inject
    lateinit var userPreferences: UserPreferencesManager

    private var sitterId: String? = null
    private var bookingId: String? = null
    private var currentBooking: SitterBookingResponse? = null

    override fun getViewBinding() = ActivitySitterBookingDetailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookingId = intent.getStringExtra(EXTRA_BOOKING_ID)
        sitterId = runBlocking { userPreferences.userId.first() }

        setupToolbar()
        setupButtons()
        observeStates()

        if (sitterId != null && bookingId != null) {
            viewModel.loadBookingDetail(sitterId!!, bookingId!!)
        } else {
            Toast.makeText(this, "預約資訊錯誤", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupButtons() {
        binding.btnConfirm.setOnClickListener {
            showConfirmDialog()
        }

        binding.btnReject.setOnClickListener {
            showRejectDialog()
        }

        binding.btnComplete.setOnClickListener {
            showCompleteDialog()
        }
    }

    private fun observeStates() {
        viewModel.bookingState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                    currentBooking = resource.data
                    displayBookingDetails(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.actionState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnConfirm.isEnabled = false
                    binding.btnReject.isEnabled = false
                    binding.btnComplete.isEnabled = false
                }
                is Resource.Success -> {
                    binding.btnConfirm.isEnabled = true
                    binding.btnReject.isEnabled = true
                    binding.btnComplete.isEnabled = true
                    Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
                    // 更新畫面
                    displayBookingDetails(resource.data)
                }
                is Resource.Error -> {
                    binding.btnConfirm.isEnabled = true
                    binding.btnReject.isEnabled = true
                    binding.btnComplete.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayBookingDetails(booking: SitterBookingResponse) {
        binding.apply {
            // 基本資訊
            tvPetName.text = booking.petName
            tvOwnerName.text = booking.username

            // 日期時間
            tvBookingDate.text = formatDateTime(booking.startTime)
            tvBookingTime.text = "${formatTime(booking.startTime)} - ${formatTime(booking.endTime)}"

            // 狀態
            tvStatus.text = booking.status.getDisplayName()
            tvStatus.setTextColor(
                ContextCompat.getColor(this@SitterBookingDetailActivity, booking.status.getColorResId())
            )

            // 價格
            booking.totalPrice?.let {
                tvPrice.text = "NT$ ${String.format("%.0f", it)}"
            }

            // 飼主備註
            if (booking.notes.isNullOrEmpty()) {
                tvNotesLabel.visibility = View.GONE
                tvNotes.visibility = View.GONE
            } else {
                tvNotesLabel.visibility = View.VISIBLE
                tvNotes.visibility = View.VISIBLE
                tvNotes.text = booking.notes
            }

            // 保母回覆
            if (booking.sitterResponse.isNullOrEmpty()) {
                tvResponseLabel.visibility = View.GONE
                tvResponse.visibility = View.GONE
            } else {
                tvResponseLabel.visibility = View.VISIBLE
                tvResponse.visibility = View.VISIBLE
                tvResponse.text = booking.sitterResponse
            }

            // 根據狀態顯示按鈕
            updateActionButtons(booking.status)
        }
    }

    private fun updateActionButtons(status: BookingStatus) {
        binding.apply {
            when (status) {
                BookingStatus.PENDING -> {
                    btnConfirm.visibility = View.VISIBLE
                    btnReject.visibility = View.VISIBLE
                    btnComplete.visibility = View.GONE
                }
                BookingStatus.CONFIRMED -> {
                    btnConfirm.visibility = View.GONE
                    btnReject.visibility = View.GONE
                    btnComplete.visibility = View.VISIBLE
                }
                else -> {
                    btnConfirm.visibility = View.GONE
                    btnReject.visibility = View.GONE
                    btnComplete.visibility = View.GONE
                }
            }
        }
    }

    private fun showConfirmDialog() {
        val input = EditText(this)
        input.hint = "回覆訊息（選填）"

        AlertDialog.Builder(this)
            .setTitle("確認接受預約")
            .setMessage("確定要接受這個預約嗎？")
            .setView(input)
            .setPositiveButton("確認") { _, _ ->
                val response = input.text.toString().takeIf { it.isNotBlank() }
                sitterId?.let { sid ->
                    bookingId?.let { bid ->
                        viewModel.confirmBooking(sid, bid, response)
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showRejectDialog() {
        val input = EditText(this)
        input.hint = "拒絕原因（選填）"

        AlertDialog.Builder(this)
            .setTitle("拒絕預約")
            .setMessage("確定要拒絕這個預約嗎？")
            .setView(input)
            .setPositiveButton("確認") { _, _ ->
                val reason = input.text.toString().takeIf { it.isNotBlank() }
                sitterId?.let { sid ->
                    bookingId?.let { bid ->
                        viewModel.rejectBooking(sid, bid, reason)
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showCompleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("完成預約")
            .setMessage("確定要將此預約標記為已完成嗎？")
            .setPositiveButton("確認") { _, _ ->
                sitterId?.let { sid ->
                    bookingId?.let { bid ->
                        viewModel.completeBooking(sid, bid)
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun formatDateTime(dateTimeStr: String): String {
        return try {
            val dateTime = LocalDateTime.parse(dateTimeStr)
            dateTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 (E)", Locale.TAIWAN))
        } catch (e: Exception) {
            dateTimeStr.substring(0, 10)
        }
    }

    private fun formatTime(dateTimeStr: String): String {
        return try {
            val dateTime = LocalDateTime.parse(dateTimeStr)
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            dateTimeStr.substring(11, 16)
        }
    }

    companion object {
        private const val EXTRA_BOOKING_ID = "booking_id"

        fun start(context: Context, bookingId: String) {
            val intent = Intent(context, SitterBookingDetailActivity::class.java)
            intent.putExtra(EXTRA_BOOKING_ID, bookingId)
            context.startActivity(intent)
        }
    }
}
