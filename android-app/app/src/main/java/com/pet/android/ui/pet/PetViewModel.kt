package com.pet.android.ui.pet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.model.Pet
import com.pet.android.data.repository.PetRepository
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _petsState = MutableLiveData<Resource<List<Pet>>>()
    val petsState: LiveData<Resource<List<Pet>>> = _petsState

    fun loadPets() {
        viewModelScope.launch {
            _petsState.value = Resource.Loading
            _petsState.value = petRepository.getAllPets()
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            val result = petRepository.deletePet(petId)
            if (result is Resource.Success) {
                loadPets()
            }
        }
    }
}
