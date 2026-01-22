package com.pet.android.ui.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.api.PetApi
import com.pet.android.data.model.AvailableSitterResponse
import com.pet.android.data.model.Pet
import com.pet.android.data.repository.BookingRepository
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class BookingHomeViewModel @Inject constructor(
    private val petApi: PetApi,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _petsState = MutableLiveData<Resource<List<Pet>>>()
    val petsState: LiveData<Resource<List<Pet>>> = _petsState

    private val _sittersState = MutableLiveData<Resource<List<AvailableSitterResponse>>>()
    val sittersState: LiveData<Resource<List<AvailableSitterResponse>>> = _sittersState

    var selectedPet: Pet? = null
    var selectedDate: LocalDate? = null
    var selectedDuration: Duration = Duration.FULL_DAY

    enum class Duration(val hours: Int) {
        HALF_DAY(4),
        FULL_DAY(8),
        OVERNIGHT(24)
    }

    fun loadUserPets(userId: String) {
        _petsState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = petApi.getPetsByUser(userId)
                val pets = response.data ?: emptyList()
                _petsState.value = Resource.Success(pets)
            } catch (e: Exception) {
                _petsState.value = Resource.Error(e.message ?: "Failed to load pets")
            }
        }
    }

    fun searchAvailableSitters() {
        val date = selectedDate ?: return

        _sittersState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val dateStr = date.format(DateTimeFormatter.ISO_DATE)
                val response = bookingRepository.getAvailableSitters(dateStr)
                val sitters = response.data ?: emptyList()
                _sittersState.value = Resource.Success(sitters)
            } catch (e: Exception) {
                _sittersState.value = Resource.Error(e.message ?: "Failed to load sitters")
            }
        }
    }
}
