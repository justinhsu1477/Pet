package com.pet.android.ui.sitter.statistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pet.android.data.model.SimpleRating
import com.pet.android.databinding.ItemSimpleRatingBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 簡易評價列表 Adapter
 */
class SimpleRatingAdapter : ListAdapter<SimpleRating, SimpleRatingAdapter.SimpleRatingViewHolder>(
    SimpleRatingDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRatingViewHolder {
        val binding = ItemSimpleRatingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SimpleRatingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimpleRatingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SimpleRatingViewHolder(
        private val binding: ItemSimpleRatingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rating: SimpleRating) {
            // 設定用戶名稱（匿名則顯示「匿名用戶」）
            binding.tvUserName.text = if (rating.isAnonymous || rating.userName.isNullOrEmpty()) {
                "匿名用戶"
            } else {
                rating.userName
            }

            // 設定評分
            binding.ratingBar.rating = rating.overallRating.toFloat()
            binding.tvRating.text = rating.overallRating.toString()

            // 設定評價內容
            if (rating.comment.isNullOrEmpty()) {
                binding.tvComment.visibility = View.GONE
            } else {
                binding.tvComment.visibility = View.VISIBLE
                binding.tvComment.text = rating.comment
            }

            // 設定日期
            try {
                val dateTime = LocalDateTime.parse(rating.createdAt)
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                binding.tvDate.text = dateTime.format(formatter)
            } catch (e: Exception) {
                binding.tvDate.text = rating.createdAt
            }
        }
    }

    class SimpleRatingDiffCallback : DiffUtil.ItemCallback<SimpleRating>() {
        override fun areItemsTheSame(oldItem: SimpleRating, newItem: SimpleRating): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SimpleRating, newItem: SimpleRating): Boolean {
            return oldItem == newItem
        }
    }
}
