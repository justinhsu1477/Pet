package com.pet.android.ui.sitter.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.model.SitterBookingResponse
import com.pet.android.data.repository.SitterBookingRepository
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SitterBookingDetailViewModel @Inject constructor(
    private val repository: SitterBookingRepository
) : ViewModel() {

    private val _bookingState = MutableLiveData<Resource<SitterBookingResponse>>()
    val bookingState: LiveData<Resource<SitterBookingResponse>> = _bookingState

    private val _actionState = MutableLiveData<Resource<SitterBookingResponse>>()
    val actionState: LiveData<Resource<SitterBookingResponse>> = _actionState

    fun loadBookingDetail(sitterId: String, bookingId: String) {
        viewModelScope.launch {
            _bookingState.value = Resource.Loading
            try {
                val response = repository.getBookingDetail(sitterId, bookingId)
                if (response.success && response.data != null) {
                    _bookingState.value = Resource.Success(response.data)
                } else {
                    _bookingState.value = Resource.Error(response.message ?: "載入失敗")
                }
            } catch (e: Exception) {
                _bookingState.value = Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    fun confirmBooking(sitterId: String, bookingId: String, response: String?) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            try {
                val apiResponse = repository.confirmBooking(sitterId, bookingId, response)
                if (apiResponse.success && apiResponse.data != null) {
                    _actionState.value = Resource.Success(apiResponse.data)
                    _bookingState.value = Resource.Success(apiResponse.data)
                } else {
                    _actionState.value = Resource.Error(apiResponse.message ?: "確認失敗")
                }
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    fun rejectBooking(sitterId: String, bookingId: String, reason: String?) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            try {
                val apiResponse = repository.rejectBooking(sitterId, bookingId, reason)
                if (apiResponse.success && apiResponse.data != null) {
                    _actionState.value = Resource.Success(apiResponse.data)
                    _bookingState.value = Resource.Success(apiResponse.data)
                } else {
                    _actionState.value = Resource.Error(apiResponse.message ?: "拒絕失敗")
                }
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    fun completeBooking(sitterId: String, bookingId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            try {
                val apiResponse = repository.completeBooking(sitterId, bookingId)
                if (apiResponse.success && apiResponse.data != null) {
                    _actionState.value = Resource.Success(apiResponse.data)
                    _bookingState.value = Resource.Success(apiResponse.data)
                } else {
                    _actionState.value = Resource.Error(apiResponse.message ?: "完成失敗")
                }
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }
}
