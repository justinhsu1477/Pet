package com.pet.android.ui.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.model.BookingRequest
import com.pet.android.data.model.BookingResponse
import com.pet.android.data.repository.BookingRepository
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingConfirmViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _bookingState = MutableLiveData<Resource<BookingResponse>>()
    val bookingState: LiveData<Resource<BookingResponse>> = _bookingState

    fun createBooking(userId: String, request: BookingRequest) {
        _bookingState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = bookingRepository.createBooking(userId, request)
                response.data?.let {
                    _bookingState.value = Resource.Success(it)
                } ?: run {
                    _bookingState.value = Resource.Error(response.message ?: "Booking failed")
                }
            } catch (e: Exception) {
                _bookingState.value = Resource.Error(e.message ?: "Booking failed")
            }
        }
    }
}
