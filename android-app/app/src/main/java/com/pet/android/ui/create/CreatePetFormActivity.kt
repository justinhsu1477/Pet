package com.pet.android.ui.create

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.pet.android.R
import com.pet.android.data.model.*
import com.pet.android.databinding.ActivityCreatePetFormBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreatePetFormActivity : BaseActivity<ActivityCreatePetFormBinding>() {

    private val viewModel: CreatePetViewModel by viewModels()
    private lateinit var species: Species

    override fun getViewBinding() = ActivityCreatePetFormBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        species = Species.valueOf(intent.getStringExtra(EXTRA_SPECIES) ?: Species.DOG.name)

        setupToolbarNavigation(binding.toolbar)
        binding.toolbar.title = if (species == Species.DOG) {
            getString(R.string.create_dog_title)
        } else {
            getString(R.string.create_cat_title)
        }
        setupUI()
        setupObservers()
        setupSubmitButton()
    }

    private fun setupUI() {
        // Set pet icon and title
        if (species == Species.DOG) {
            binding.tvPetIcon.text = getString(R.string.emoji_dog)
            binding.tvFormTitle.text = getString(R.string.form_title_dog)
            binding.dogSpecificSection.visibility = View.VISIBLE
            binding.catSpecificSection.visibility = View.GONE
        } else {
            binding.tvPetIcon.text = getString(R.string.emoji_cat)
            binding.tvFormTitle.text = getString(R.string.form_title_cat)
            binding.dogSpecificSection.visibility = View.GONE
            binding.catSpecificSection.visibility = View.VISIBLE
        }
    }

    private fun setupObservers() {
        viewModel.createDogState.observe(this) { resource ->
            handleCreateResult(resource)
        }

        viewModel.createCatState.observe(this) { resource ->
            handleCreateResult(resource)
        }
    }

    private fun <T> handleCreateResult(resource: Resource<T>) {
        when (resource) {
            is Resource.Loading -> {
                showLoading(true)
            }
            is Resource.Success -> {
                showLoading(false)
                Toast.makeText(
                    this,
                    if (species == Species.DOG) R.string.dog_created_success else R.string.cat_created_success,
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK)
                finish()
            }
            is Resource.Error -> {
                showLoading(false)
                Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressIndicator.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !show
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitForm()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate name (required)
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_name_required)
            isValid = false
        } else {
            binding.tilName.error = null
        }

        return isValid
    }

    private fun submitForm() {
        val name = binding.etName.text.toString().trim()
        val age = binding.etAge.text.toString().toIntOrNull()
        val breed = binding.etBreed.text.toString().trim().takeIf { it.isNotEmpty() }
        val vaccineStatus = binding.etVaccineStatus.text.toString().trim().takeIf { it.isNotEmpty() }
        val specialNeeds = binding.etSpecialNeeds.text.toString().trim().takeIf { it.isNotEmpty() }
        val isNeutered = binding.switchNeutered.isChecked

        val gender = when {
            binding.chipMale.isChecked -> Gender.MALE
            binding.chipFemale.isChecked -> Gender.FEMALE
            else -> null
        }

        if (species == Species.DOG) {
            submitDog(
                name, age, breed, gender,
                specialNeeds, isNeutered, vaccineStatus
            )
        } else {
            submitCat(
                name, age, breed, gender,
                specialNeeds, isNeutered, vaccineStatus
            )
        }
    }

    private fun submitDog(
        name: String,
        age: Int?,
        breed: String?,
        gender: Gender?,
        specialNeeds: String?,
        isNeutered: Boolean?,
        vaccineStatus: String?
    ) {
        val size = when {
            binding.chipSizeSmall.isChecked -> DogSize.SMALL
            binding.chipSizeMedium.isChecked -> DogSize.MEDIUM
            binding.chipSizeLarge.isChecked -> DogSize.LARGE
            binding.chipSizeGiant.isChecked -> DogSize.GIANT
            else -> null
        }

        val trainingLevel = when {
            binding.chipTrainingNone.isChecked -> TrainingLevel.NONE
            binding.chipTrainingBasic.isChecked -> TrainingLevel.BASIC
            binding.chipTrainingIntermediate.isChecked -> TrainingLevel.INTERMEDIATE
            binding.chipTrainingAdvanced.isChecked -> TrainingLevel.ADVANCED
            else -> null
        }

        val isWalkRequired = binding.switchWalkRequired.isChecked
        val walkFrequency = binding.etWalkFrequency.text.toString().toIntOrNull()
        val isFriendlyWithDogs = binding.switchFriendlyDogs.isChecked
        val isFriendlyWithPeople = binding.switchFriendlyPeople.isChecked
        val isFriendlyWithChildren = binding.switchFriendlyChildren.isChecked

        viewModel.createDog(
            name = name,
            age = age,
            breed = breed,
            gender = gender,
            specialNeeds = specialNeeds,
            isNeutered = isNeutered,
            vaccineStatus = vaccineStatus,
            size = size,
            isWalkRequired = isWalkRequired,
            walkFrequencyPerDay = walkFrequency,
            trainingLevel = trainingLevel,
            isFriendlyWithDogs = isFriendlyWithDogs,
            isFriendlyWithPeople = isFriendlyWithPeople,
            isFriendlyWithChildren = isFriendlyWithChildren
        )
    }

    private fun submitCat(
        name: String,
        age: Int?,
        breed: String?,
        gender: Gender?,
        specialNeeds: String?,
        isNeutered: Boolean?,
        vaccineStatus: String?
    ) {
        val isIndoor = binding.switchIndoor.isChecked

        val litterBoxType = when {
            binding.chipLitterOpen.isChecked -> LitterBoxType.OPEN
            binding.chipLitterCovered.isChecked -> LitterBoxType.COVERED
            binding.chipLitterAutomatic.isChecked -> LitterBoxType.AUTOMATIC
            binding.chipLitterTopEntry.isChecked -> LitterBoxType.TOP_ENTRY
            else -> null
        }

        val scratchingHabit = when {
            binding.chipScratchLow.isChecked -> ScratchingHabit.LOW
            binding.chipScratchModerate.isChecked -> ScratchingHabit.MODERATE
            binding.chipScratchHigh.isChecked -> ScratchingHabit.HIGH
            else -> null
        }

        viewModel.createCat(
            name = name,
            age = age,
            breed = breed,
            gender = gender,
            specialNeeds = specialNeeds,
            isNeutered = isNeutered,
            vaccineStatus = vaccineStatus,
            isIndoor = isIndoor,
            litterBoxType = litterBoxType,
            scratchingHabit = scratchingHabit
        )
    }

    companion object {
        const val EXTRA_SPECIES = "extra_species"
    }
}
