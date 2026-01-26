package com.pet.android.ui.user.pet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.api.PetApi
import com.pet.android.data.model.Pet
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPetViewModel @Inject constructor(
    private val petApi: PetApi,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _petsState = MutableLiveData<Resource<List<Pet>>>()
    val petsState: LiveData<Resource<List<Pet>>> = _petsState

    private val _deleteState = MutableLiveData<Resource<Unit>>()
    val deleteState: LiveData<Resource<Unit>> = _deleteState

    fun loadUserPets() {
        _petsState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val userId = userPreferencesManager.userId.first()
                if (userId == null) {
                    _petsState.value = Resource.Error("請先登入")
                    return@launch
                }
                val response = petApi.getPetsByUser(userId)
                val pets = response.data ?: emptyList()
                _petsState.value = Resource.Success(pets)
            } catch (e: Exception) {
                _petsState.value = Resource.Error(e.message ?: "載入寵物失敗")
            }
        }
    }

    fun deletePet(petId: String) {
        _deleteState.value = Resource.Loading
        viewModelScope.launch {
            try {
                petApi.deletePet(petId)
                _deleteState.value = Resource.Success(Unit)
                // Reload the list after deletion
                loadUserPets()
            } catch (e: Exception) {
                _deleteState.value = Resource.Error(e.message ?: "刪除寵物失敗")
            }
        }
    }
}
