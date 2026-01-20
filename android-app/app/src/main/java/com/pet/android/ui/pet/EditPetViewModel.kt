package com.pet.android.ui.pet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.model.*
import com.pet.android.data.repository.CatRepository
import com.pet.android.data.repository.DogRepository
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPetViewModel @Inject constructor(
    private val dogRepository: DogRepository,
    private val catRepository: CatRepository
) : ViewModel() {

    private val _dogState = MutableLiveData<Resource<DogRequest>>()
    val dogState: LiveData<Resource<DogRequest>> = _dogState

    private val _catState = MutableLiveData<Resource<CatRequest>>()
    val catState: LiveData<Resource<CatRequest>> = _catState

    private val _updateDogState = MutableLiveData<Resource<DogRequest>>()
    val updateDogState: LiveData<Resource<DogRequest>> = _updateDogState

    private val _updateCatState = MutableLiveData<Resource<CatRequest>>()
    val updateCatState: LiveData<Resource<CatRequest>> = _updateCatState

    fun loadDog(id: String) {
        viewModelScope.launch {
            _dogState.value = Resource.Loading
            _dogState.value = dogRepository.getDogById(id)
        }
    }

    fun loadCat(id: String) {
        viewModelScope.launch {
            _catState.value = Resource.Loading
            _catState.value = catRepository.getCatById(id)
        }
    }

    fun updateDog(
        id: String,
        name: String,
        age: Int?,
        breed: String?,
        gender: Gender?,
        ownerName: String,
        ownerPhone: String,
        specialNeeds: String?,
        isNeutered: Boolean?,
        vaccineStatus: String?,
        size: DogSize?,
        isWalkRequired: Boolean?,
        walkFrequencyPerDay: Int?,
        trainingLevel: TrainingLevel?,
        isFriendlyWithDogs: Boolean?,
        isFriendlyWithPeople: Boolean?,
        isFriendlyWithChildren: Boolean?
    ) {
        viewModelScope.launch {
            _updateDogState.value = Resource.Loading
            val dog = DogRequest(
                id = id,
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
                walkFrequencyPerDay = walkFrequencyPerDay,
                trainingLevel = trainingLevel,
                isFriendlyWithDogs = isFriendlyWithDogs,
                isFriendlyWithPeople = isFriendlyWithPeople,
                isFriendlyWithChildren = isFriendlyWithChildren
            )
            _updateDogState.value = dogRepository.updateDog(id, dog)
        }
    }

    fun updateCat(
        id: String,
        name: String,
        age: Int?,
        breed: String?,
        gender: Gender?,
        ownerName: String,
        ownerPhone: String,
        specialNeeds: String?,
        isNeutered: Boolean?,
        vaccineStatus: String?,
        isIndoor: Boolean?,
        litterBoxType: LitterBoxType?,
        scratchingHabit: ScratchingHabit?
    ) {
        viewModelScope.launch {
            _updateCatState.value = Resource.Loading
            val cat = CatRequest(
                id = id,
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
            _updateCatState.value = catRepository.updateCat(id, cat)
        }
    }
}
