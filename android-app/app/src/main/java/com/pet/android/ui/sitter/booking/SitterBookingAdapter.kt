package com.pet.android.ui.sitter.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pet.android.data.model.SitterBookingResponse
import com.pet.android.databinding.ItemSitterBookingBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class SitterBookingAdapter(
    private val onItemClick: (SitterBookingResponse) -> Unit
) : ListAdapter<SitterBookingResponse, SitterBookingAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemSitterBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookingViewHolder(
        private val binding: ItemSitterBookingBinding,
        private val onItemClick: (SitterBookingResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: SitterBookingResponse) {
            binding.apply {
                // 寵物名稱和飼主
                tvPetName.text = booking.petName
                tvOwnerName.text = "飼主：${booking.username}"

                // 日期時間
                tvBookingDate.text = formatDateTime(booking.startTime)
                tvBookingTime.text = "${formatTime(booking.startTime)} - ${formatTime(booking.endTime)}"

                // 狀態
                tvStatus.text = booking.status.getDisplayName()
                tvStatus.setTextColor(
                    ContextCompat.getColor(root.context, booking.status.getColorResId())
                )

                // 價格
                booking.totalPrice?.let {
                    tvPrice.text = "NT$ ${String.format("%.0f", it)}"
                }

                // 備註
                booking.notes?.let {
                    tvNotes.text = "備註：$it"
                } ?: run {
                    tvNotes.text = ""
                }

                root.setOnClickListener { onItemClick(booking) }
            }
        }

        private fun formatDateTime(dateTimeStr: String): String {
            return try {
                val dateTime = LocalDateTime.parse(dateTimeStr)
                dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd (E)", Locale.TAIWAN))
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
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<SitterBookingResponse>() {
        override fun areItemsTheSame(
            oldItem: SitterBookingResponse,
            newItem: SitterBookingResponse
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: SitterBookingResponse,
            newItem: SitterBookingResponse
        ): Boolean = oldItem == newItem
    }
}
