package com.pet.android.ui.sitter.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pet.android.data.model.DailyRevenue
import com.pet.android.databinding.ItemDailyRevenueBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * 每日收入列表 Adapter
 */
class DailyRevenueAdapter : ListAdapter<DailyRevenue, DailyRevenueAdapter.DailyRevenueViewHolder>(
    DailyRevenueDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyRevenueViewHolder {
        val binding = ItemDailyRevenueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DailyRevenueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyRevenueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DailyRevenueViewHolder(
        private val binding: ItemDailyRevenueBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val numberFormat = NumberFormat.getNumberInstance(Locale.TAIWAN)

        fun bind(dailyRevenue: DailyRevenue) {
            binding.tvDate.text = dailyRevenue.date
            binding.tvBookingCount.text = "${dailyRevenue.bookingCount} 筆"
            binding.tvRevenue.text = "NT$ ${numberFormat.format(dailyRevenue.revenue.toInt())}"
        }
    }

    class DailyRevenueDiffCallback : DiffUtil.ItemCallback<DailyRevenue>() {
        override fun areItemsTheSame(oldItem: DailyRevenue, newItem: DailyRevenue): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DailyRevenue, newItem: DailyRevenue): Boolean {
            return oldItem == newItem
        }
    }
}
