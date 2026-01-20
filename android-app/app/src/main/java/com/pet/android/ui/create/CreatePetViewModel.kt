package com.pet.android.ui.create

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
class CreatePetViewModel @Inject constructor(
    private val catRepository: CatRepository,
    private val dogRepository: DogRepository
) : ViewModel() {

    private val _createDogState = MutableLiveData<Resource<DogRequest>>()
    val createDogState: LiveData<Resource<DogRequest>> = _createDogState

    private val _createCatState = MutableLiveData<Resource<CatRequest>>()
    val createCatState: LiveData<Resource<CatRequest>> = _createCatState

    fun createDog(
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
        _createDogState.value = Resource.Loading
        viewModelScope.launch {
            val dogRequest = DogRequest(
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
            _createDogState.value = dogRepository.createDog(dogRequest)
        }
    }

    fun createCat(
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
        _createCatState.value = Resource.Loading
        viewModelScope.launch {
            val catRequest = CatRequest(
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
            _createCatState.value = catRepository.createCat(catRequest)
        }
    }
}
