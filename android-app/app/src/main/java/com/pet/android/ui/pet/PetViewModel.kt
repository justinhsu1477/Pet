package com.pet.android.ui.pet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.model.CatRequest
import com.pet.android.data.model.DogRequest
import com.pet.android.data.model.Pet
import com.pet.android.data.repository.CatRepository
import com.pet.android.data.repository.DogRepository
import com.pet.android.data.repository.PetRepository
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PetFilter {
    ALL, DOG, CAT
}

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val dogRepository: DogRepository,
    private val catRepository: CatRepository
) : ViewModel() {

    private val _petsState = MutableLiveData<Resource<List<Pet>>>()
    val petsState: LiveData<Resource<List<Pet>>> = _petsState

    private val _currentFilter = MutableLiveData(PetFilter.ALL)
    val currentFilter: LiveData<PetFilter> = _currentFilter

    private var allPets: List<Pet> = emptyList()

    fun loadPets() {
        viewModelScope.launch {
            _petsState.value = Resource.Loading

            // Load all pets from all sources
            val combinedPets = mutableListOf<Pet>()

            // Load dogs
            when (val dogsResult = dogRepository.getAllDogs()) {
                is Resource.Success -> {
                    dogsResult.data.forEach { dog ->
                        combinedPets.add(dog.toPet())
                    }
                }
                is Resource.Error -> { /* ignore individual errors */ }
                is Resource.Loading -> { /* ignore */ }
            }

            // Load cats
            when (val catsResult = catRepository.getAllCats()) {
                is Resource.Success -> {
                    catsResult.data.forEach { cat ->
                        combinedPets.add(cat.toPet())
                    }
                }
                is Resource.Error -> { /* ignore individual errors */ }
                is Resource.Loading -> { /* ignore */ }
            }

            allPets = combinedPets
            applyFilter(_currentFilter.value ?: PetFilter.ALL)
        }
    }

    fun setFilter(filter: PetFilter) {
        _currentFilter.value = filter
        applyFilter(filter)
    }

    private fun applyFilter(filter: PetFilter) {
        val filteredPets = when (filter) {
            PetFilter.ALL -> allPets
            PetFilter.DOG -> allPets.filter { it.type.equals("DOG", ignoreCase = true) || it.type == "狗" }
            PetFilter.CAT -> allPets.filter { it.type.equals("CAT", ignoreCase = true) || it.type == "貓" }
        }
        _petsState.value = Resource.Success(filteredPets)
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            val result = petRepository.deletePet(petId)
            if (result is Resource.Success) {
                loadPets()
            }
        }
    }

    // Extension function to convert DogRequest to Pet
    private fun DogRequest.toPet(): Pet {
        return Pet(
            id = this.id,
            name = this.name,
            type = "DOG",
            age = this.age ?: 0,
            breed = this.breed,
            ownerName = this.ownerName,
            ownerPhone = this.ownerPhone,
            specialNeeds = this.specialNeeds
        )
    }

    // Extension function to convert CatRequest to Pet
    private fun CatRequest.toPet(): Pet {
        return Pet(
            id = this.id,
            name = this.name,
            type = "CAT",
            age = this.age ?: 0,
            breed = this.breed,
            ownerName = this.ownerName,
            ownerPhone = this.ownerPhone,
            specialNeeds = this.specialNeeds
        )
    }
}
