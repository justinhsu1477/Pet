package com.pet.android.ui.sitter.rating

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pet.android.data.model.SitterRatingResponse
import com.pet.android.databinding.ItemSitterRatingBinding

class SitterRatingAdapter :
    ListAdapter<SitterRatingResponse, SitterRatingAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSitterRatingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(private val b: ItemSitterRatingBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: SitterRatingResponse) {
            val stars = item.overallRating.coerceIn(0, 5)
            b.tvRating.text = "★".repeat(stars) + " (${item.overallRating})"
            b.tvComment.text = item.comment ?: "—"
            if (!item.reply.isNullOrBlank()) {
                b.tvReply.visibility = View.VISIBLE
                b.tvReply.text = "保母回覆：${item.reply}"
            } else {
                b.tvReply.visibility = View.GONE
            }
            // 簡單格式化時間
            b.tvCreatedAt.text = item.createdAt.take(19).replace('T', ' ')
        }
    }

    class Diff : DiffUtil.ItemCallback<SitterRatingResponse>() {
        override fun areItemsTheSame(o: SitterRatingResponse, n: SitterRatingResponse) = o.id == n.id
        override fun areContentsTheSame(o: SitterRatingResponse, n: SitterRatingResponse) = o == n
    }
}