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
