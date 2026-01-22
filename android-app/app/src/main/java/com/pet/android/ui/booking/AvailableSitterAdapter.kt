package com.pet.android.ui.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pet.android.R
import com.pet.android.data.model.AvailableSitterResponse
import com.pet.android.databinding.ItemAvailableSitterBinding

class AvailableSitterAdapter(
    private val onViewRatings: (AvailableSitterResponse) -> Unit,
    private val onSelect: (AvailableSitterResponse) -> Unit
) : ListAdapter<AvailableSitterResponse, AvailableSitterAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAvailableSitterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAvailableSitterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sitter: AvailableSitterResponse) {
            val context = binding.root.context

            // Avatar
            binding.tvAvatar.text = sitter.name.firstOrNull()?.toString() ?: "?"

            // Name
            binding.tvName.text = sitter.name

            // Rating
            val rating = sitter.averageRating ?: 0.0
            binding.tvRating.text = String.format("%.1f", rating)
            binding.tvRatingCount.text = "(${sitter.ratingCount ?: 0})"

            // Completed bookings
            binding.tvCompletedBookings.text = context.getString(
                R.string.completed_bookings_format,
                sitter.completedBookings ?: 0
            )

            // Experience
            binding.tvExperience.text = sitter.experience ?: ""

            // Buttons
            binding.btnViewRatings.setOnClickListener { onViewRatings(sitter) }
            binding.btnSelect.setOnClickListener { onSelect(sitter) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AvailableSitterResponse>() {
        override fun areItemsTheSame(
            oldItem: AvailableSitterResponse,
            newItem: AvailableSitterResponse
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: AvailableSitterResponse,
            newItem: AvailableSitterResponse
        ): Boolean = oldItem == newItem
    }
}
