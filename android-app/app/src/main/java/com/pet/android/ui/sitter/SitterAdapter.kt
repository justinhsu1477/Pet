package com.pet.android.ui.sitter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pet.android.data.model.Sitter
import com.pet.android.databinding.ItemSitterBinding

class SitterAdapter(
    private val onItemClick: (Sitter) -> Unit
) : ListAdapter<Sitter, SitterAdapter.SitterViewHolder>(SitterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SitterViewHolder {
        val binding = ItemSitterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SitterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SitterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SitterViewHolder(
        private val binding: ItemSitterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sitter: Sitter) {
            binding.apply {
                tvSitterName.text = sitter.name
                tvSitterPhone.text = sitter.phone
                tvSitterExperience.text = sitter.experience ?: "無經驗描述"

                // 設置評分
                if (sitter.averageRating != null && sitter.averageRating > 0) {
                    ratingBar.rating = sitter.averageRating.toFloat()
                    ratingBar.visibility = android.view.View.VISIBLE
                    tvRatingScore.text = String.format("%.1f", sitter.averageRating)
                    tvRatingScore.visibility = android.view.View.VISIBLE
                    tvRatingCount.text = "(${sitter.ratingCount ?: 0})"
                    tvRatingCount.visibility = android.view.View.VISIBLE
                } else {
                    ratingBar.visibility = android.view.View.GONE
                    tvRatingScore.visibility = android.view.View.GONE
                    tvRatingCount.text = "(尚無評價)"
                    tvRatingCount.visibility = android.view.View.VISIBLE
                }

                // 設置完成訂單數
                if (sitter.completedBookings != null && sitter.completedBookings > 0) {
                    chipCompletedBookings.text = "已完成 ${sitter.completedBookings} 次服務"
                    chipCompletedBookings.visibility = android.view.View.VISIBLE
                } else {
                    chipCompletedBookings.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onItemClick(sitter)
                }
            }
        }
    }

    class SitterDiffCallback : DiffUtil.ItemCallback<Sitter>() {
        override fun areItemsTheSame(oldItem: Sitter, newItem: Sitter): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Sitter, newItem: Sitter): Boolean {
            return oldItem == newItem
        }
    }
}
