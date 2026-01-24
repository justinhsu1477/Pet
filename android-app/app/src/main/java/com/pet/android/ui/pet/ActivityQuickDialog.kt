package com.pet.android.ui.pet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pet.android.R
import com.pet.android.data.model.Pet
import com.pet.android.data.model.PetActivityResponse
import com.pet.android.databinding.DialogActivityQuickBinding
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ActivityQuickDialog : DialogFragment() {

    private var _binding: DialogActivityQuickBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PetViewModel by viewModels({ requireActivity() })

    private var pet: Pet? = null
    private var currentActivity: PetActivityResponse? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogActivityQuickBinding.inflate(layoutInflater)

        pet = arguments?.let {
            Pet(
                id = it.getString(ARG_PET_ID),
                name = it.getString(ARG_PET_NAME) ?: "",
                type = it.getString(ARG_PET_TYPE) ?: "",
                age = it.getInt(ARG_PET_AGE),
                breed = it.getString(ARG_PET_BREED),
                specialNeeds = it.getString(ARG_SPECIAL_NEEDS)
            )
        }

        setupUI()
        setupObservers()
        setupListeners()

        pet?.id?.let { viewModel.loadTodayActivity(it) }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.confirm) { _, _ ->
                saveActivity()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun setupUI() {
        pet?.let { p ->
            val isDog = p.type.equals("DOG", ignoreCase = true) || p.type == "狗"
            binding.tvPetIcon.text = if (isDog) "🐶" else "🐱"
            binding.tvPetName.text = p.name

            val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(Date())
        }
    }

    private fun setupObservers() {
        viewModel.todayActivityState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    currentActivity = resource.data
                    updateActivityUI(resource.data)
                }
                is Resource.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.recordActivityState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                }
                is Resource.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateActivityUI(activity: PetActivityResponse) {
        binding.switchWalked.isChecked = activity.walked == true
        binding.switchFed.isChecked = activity.fed == true

        activity.walkTime?.let {
            binding.tvWalkTime.visibility = View.VISIBLE
            binding.tvWalkTime.text = getString(R.string.walked_at, formatTime(it))
        } ?: run {
            binding.tvWalkTime.visibility = View.GONE
        }

        activity.feedTime?.let {
            binding.tvFeedTime.visibility = View.VISIBLE
            binding.tvFeedTime.text = getString(R.string.fed_at, formatTime(it))
        } ?: run {
            binding.tvFeedTime.visibility = View.GONE
        }
    }

    private fun formatTime(dateTimeStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateTimeStr)
            date?.let { outputFormat.format(it) } ?: dateTimeStr
        } catch (e: Exception) {
            dateTimeStr
        }
    }

    private fun setupListeners() {
        binding.switchWalked.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && binding.tvWalkTime.visibility == View.GONE) {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                binding.tvWalkTime.text = getString(R.string.walked_at, timeFormat.format(Date()))
                binding.tvWalkTime.visibility = View.VISIBLE
            } else if (!isChecked) {
                binding.tvWalkTime.visibility = View.GONE
            }
        }

        binding.switchFed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && binding.tvFeedTime.visibility == View.GONE) {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                binding.tvFeedTime.text = getString(R.string.fed_at, timeFormat.format(Date()))
                binding.tvFeedTime.visibility = View.VISIBLE
            } else if (!isChecked) {
                binding.tvFeedTime.visibility = View.GONE
            }
        }
    }

    private fun saveActivity() {
        pet?.id?.let { petId ->
            viewModel.recordActivity(
                petId = petId,
                walked = binding.switchWalked.isChecked,
                fed = binding.switchFed.isChecked
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ActivityQuickDialog"
        private const val ARG_PET_ID = "pet_id"
        private const val ARG_PET_NAME = "pet_name"
        private const val ARG_PET_TYPE = "pet_type"
        private const val ARG_PET_AGE = "pet_age"
        private const val ARG_PET_BREED = "pet_breed"
        private const val ARG_SPECIAL_NEEDS = "special_needs"

        fun newInstance(pet: Pet): ActivityQuickDialog {
            return ActivityQuickDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_PET_ID, pet.id)
                    putString(ARG_PET_NAME, pet.name)
                    putString(ARG_PET_TYPE, pet.type)
                    putInt(ARG_PET_AGE, pet.age)
                    putString(ARG_PET_BREED, pet.breed)
                    putString(ARG_SPECIAL_NEEDS, pet.specialNeeds)
                }
            }
        }
    }
}
