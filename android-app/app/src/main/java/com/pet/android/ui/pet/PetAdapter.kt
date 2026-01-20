package com.pet.android.ui.pet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pet.android.R
import com.pet.android.data.model.Pet
import com.pet.android.databinding.ItemPetBinding

class PetAdapter(
    private val onItemClick: (Pet) -> Unit,
    private val onItemLongClick: (Pet) -> Unit = {}
) : ListAdapter<Pet, PetAdapter.PetViewHolder>(PetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PetViewHolder(
        private val binding: ItemPetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {
            val context = binding.root.context
            val isDog = pet.type.equals("DOG", ignoreCase = true) || pet.type == "狗"

            binding.apply {
                // Set pet icon
                tvPetIcon.text = if (isDog) "🐶" else "🐱"

                // Set pet name
                tvPetName.text = pet.name

                // Set badge
                tvPetBadge.text = if (isDog) {
                    context.getString(R.string.filter_dog)
                } else {
                    context.getString(R.string.filter_cat)
                }
                tvPetBadge.setChipBackgroundColorResource(
                    if (isDog) R.color.pet_orange_light else R.color.pet_green_light
                )

                // Set breed and age
                val breedText = pet.breed ?: if (isDog) "狗狗" else "貓咪"
                tvPetType.text = "$breedText · ${pet.age}歲"

                // Set owner info
                tvOwnerInfo.text = pet.ownerName

                root.setOnClickListener {
                    onItemClick(pet)
                }

                root.setOnLongClickListener {
                    onItemLongClick(pet)
                    true
                }
            }
        }
    }

    class PetDiffCallback : DiffUtil.ItemCallback<Pet>() {
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem == newItem
        }
    }
}
