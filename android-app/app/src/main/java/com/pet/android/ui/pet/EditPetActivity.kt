package com.pet.android.ui.pet

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.pet.android.R
import com.pet.android.data.model.*
import com.pet.android.databinding.ActivityEditPetBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditPetActivity : BaseActivity<ActivityEditPetBinding>() {

    private val viewModel: EditPetViewModel by viewModels()

    private var petId: String = ""
    private var petType: String = ""

    override fun getViewBinding() = ActivityEditPetBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        petId = intent.getStringExtra(EXTRA_PET_ID) ?: ""
        petType = intent.getStringExtra(EXTRA_PET_TYPE) ?: ""

        if (petId.isEmpty()) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupUI()
        setupObservers()
        setupSubmitButton()
        loadPetData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackButtonPressed() }
        binding.toolbar.title = if (petType.equals("DOG", ignoreCase = true)) {
            getString(R.string.edit_dog_title)
        } else {
            getString(R.string.edit_cat_title)
        }
    }

    private fun setupUI() {
        val isDog = petType.equals("DOG", ignoreCase = true)
        if (isDog) {
            binding.tvPetIcon.text = getString(R.string.emoji_dog)
            binding.tvFormTitle.text = getString(R.string.edit_dog_form_title)
            binding.dogSpecificSection.visibility = View.VISIBLE
            binding.catSpecificSection.visibility = View.GONE
        } else {
            binding.tvPetIcon.text = getString(R.string.emoji_cat)
            binding.tvFormTitle.text = getString(R.string.edit_cat_form_title)
            binding.dogSpecificSection.visibility = View.GONE
            binding.catSpecificSection.visibility = View.VISIBLE
        }
    }

    private fun loadPetData() {
        if (petType.equals("DOG", ignoreCase = true)) {
            viewModel.loadDog(petId)
        } else {
            viewModel.loadCat(petId)
        }
    }

    private fun setupObservers() {
        viewModel.dogState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    populateDogForm(resource.data)
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.catState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    populateCatForm(resource.data)
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.updateDogState.observe(this) { resource ->
            handleUpdateResult(resource)
        }

        viewModel.updateCatState.observe(this) { resource ->
            handleUpdateResult(resource)
        }
    }

    private fun <T> handleUpdateResult(resource: Resource<T>) {
        when (resource) {
            is Resource.Loading -> showLoading(true)
            is Resource.Success -> {
                showLoading(false)
                Toast.makeText(this, R.string.pet_updated_success, Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            is Resource.Error -> {
                showLoading(false)
                Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun populateDogForm(dog: DogRequest) {
        binding.apply {
            etName.setText(dog.name)
            etAge.setText(dog.age?.toString() ?: "")
            etBreed.setText(dog.breed ?: "")
            etOwnerName.setText(dog.ownerName)
            etOwnerPhone.setText(dog.ownerPhone)
            etVaccineStatus.setText(dog.vaccineStatus ?: "")
            etSpecialNeeds.setText(dog.specialNeeds ?: "")
            switchNeutered.isChecked = dog.isNeutered == true

            when (dog.gender) {
                Gender.MALE -> chipMale.isChecked = true
                Gender.FEMALE -> chipFemale.isChecked = true
                null -> {}
            }

            when (dog.size) {
                DogSize.SMALL -> chipSizeSmall.isChecked = true
                DogSize.MEDIUM -> chipSizeMedium.isChecked = true
                DogSize.LARGE -> chipSizeLarge.isChecked = true
                DogSize.GIANT -> chipSizeGiant.isChecked = true
                null -> {}
            }

            when (dog.trainingLevel) {
                TrainingLevel.NONE -> chipTrainingNone.isChecked = true
                TrainingLevel.BASIC -> chipTrainingBasic.isChecked = true
                TrainingLevel.INTERMEDIATE -> chipTrainingIntermediate.isChecked = true
                TrainingLevel.ADVANCED -> chipTrainingAdvanced.isChecked = true
                null -> {}
            }

            switchWalkRequired.isChecked = dog.isWalkRequired == true
            etWalkFrequency.setText(dog.walkFrequencyPerDay?.toString() ?: "")
            switchFriendlyDogs.isChecked = dog.isFriendlyWithDogs == true
            switchFriendlyPeople.isChecked = dog.isFriendlyWithPeople == true
            switchFriendlyChildren.isChecked = dog.isFriendlyWithChildren == true
        }
    }

    private fun populateCatForm(cat: CatRequest) {
        binding.apply {
            etName.setText(cat.name)
            etAge.setText(cat.age?.toString() ?: "")
            etBreed.setText(cat.breed ?: "")
            etOwnerName.setText(cat.ownerName)
            etOwnerPhone.setText(cat.ownerPhone)
            etVaccineStatus.setText(cat.vaccineStatus ?: "")
            etSpecialNeeds.setText(cat.specialNeeds ?: "")
            switchNeutered.isChecked = cat.isNeutered == true

            when (cat.gender) {
                Gender.MALE -> chipMale.isChecked = true
                Gender.FEMALE -> chipFemale.isChecked = true
                null -> {}
            }

            switchIndoor.isChecked = cat.isIndoor == true

            when (cat.litterBoxType) {
                LitterBoxType.OPEN -> chipLitterOpen.isChecked = true
                LitterBoxType.COVERED -> chipLitterCovered.isChecked = true
                LitterBoxType.AUTOMATIC -> chipLitterAutomatic.isChecked = true
                LitterBoxType.TOP_ENTRY -> chipLitterTopEntry.isChecked = true
                null -> {}
            }

            when (cat.scratchingHabit) {
                ScratchingHabit.LOW -> chipScratchLow.isChecked = true
                ScratchingHabit.MODERATE -> chipScratchModerate.isChecked = true
                ScratchingHabit.HIGH -> chipScratchHigh.isChecked = true
                null -> {}
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

        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_name_required)
            isValid = false
        } else {
            binding.tilName.error = null
        }

        val ownerName = binding.etOwnerName.text.toString().trim()
        if (ownerName.isEmpty()) {
            binding.tilOwnerName.error = getString(R.string.error_owner_name_required)
            isValid = false
        } else {
            binding.tilOwnerName.error = null
        }

        val ownerPhone = binding.etOwnerPhone.text.toString().trim()
        if (ownerPhone.isEmpty()) {
            binding.tilOwnerPhone.error = getString(R.string.error_owner_phone_required)
            isValid = false
        } else {
            binding.tilOwnerPhone.error = null
        }

        return isValid
    }

    private fun submitForm() {
        val name = binding.etName.text.toString().trim()
        val age = binding.etAge.text.toString().toIntOrNull()
        val breed = binding.etBreed.text.toString().trim().takeIf { it.isNotEmpty() }
        val ownerName = binding.etOwnerName.text.toString().trim()
        val ownerPhone = binding.etOwnerPhone.text.toString().trim()
        val vaccineStatus = binding.etVaccineStatus.text.toString().trim().takeIf { it.isNotEmpty() }
        val specialNeeds = binding.etSpecialNeeds.text.toString().trim().takeIf { it.isNotEmpty() }
        val isNeutered = binding.switchNeutered.isChecked

        val gender = when {
            binding.chipMale.isChecked -> Gender.MALE
            binding.chipFemale.isChecked -> Gender.FEMALE
            else -> null
        }

        if (petType.equals("DOG", ignoreCase = true)) {
            submitDog(name, age, breed, gender, ownerName, ownerPhone, specialNeeds, isNeutered, vaccineStatus)
        } else {
            submitCat(name, age, breed, gender, ownerName, ownerPhone, specialNeeds, isNeutered, vaccineStatus)
        }
    }

    private fun submitDog(
        name: String,
        age: Int?,
        breed: String?,
        gender: Gender?,
        ownerName: String,
        ownerPhone: String,
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

        viewModel.updateDog(
            id = petId,
            name = name,
            age = age,
            breed = breed,
            gender = gender,
            ownerName = ownerName,
            ownerPhone = ownerPhone,
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
        ownerName: String,
        ownerPhone: String,
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

        viewModel.updateCat(
            id = petId,
            name = name,
            age = age,
            breed = breed,
            gender = gender,
            ownerName = ownerName,
            ownerPhone = ownerPhone,
            specialNeeds = specialNeeds,
            isNeutered = isNeutered,
            vaccineStatus = vaccineStatus,
            isIndoor = isIndoor,
            litterBoxType = litterBoxType,
            scratchingHabit = scratchingHabit
        )
    }

    companion object {
        const val EXTRA_PET_ID = "extra_pet_id"
        const val EXTRA_PET_TYPE = "extra_pet_type"
    }
}
