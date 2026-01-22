package com.pet.android.ui.sitter.rating

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pet.android.R
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
            val context = b.root.context

            // User avatar (first character of userId or "U" for anonymous)
            val userName = if (item.userId.isNullOrBlank()) {
                context.getString(R.string.anonymous_user)
            } else {
                "User${item.userId.take(4)}"
            }
            b.tvAvatar.text = userName.first().uppercaseChar().toString()
            b.tvUserName.text = userName

            // Rating display
            b.tvRating.text = item.overallRating.toString()

            // Comment
            b.tvComment.text = item.comment ?: "—"

            // Sitter reply
            if (!item.reply.isNullOrBlank()) {
                b.layoutReply.visibility = View.VISIBLE
                b.tvReply.text = item.reply
            } else {
                b.layoutReply.visibility = View.GONE
            }

            // Format date: "2026-01-21T10:00:00" -> "2026-01-21"
            b.tvCreatedAt.text = item.createdAt.take(10)
        }
    }

    class Diff : DiffUtil.ItemCallback<SitterRatingResponse>() {
        override fun areItemsTheSame(o: SitterRatingResponse, n: SitterRatingResponse) = o.id == n.id
        override fun areContentsTheSame(o: SitterRatingResponse, n: SitterRatingResponse) = o == n
    }
}
