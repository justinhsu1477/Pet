package com.pet.android.ui.sitter.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.model.BookingStatus
import com.pet.android.data.model.SitterBookingResponse
import com.pet.android.data.repository.SitterBookingRepository
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SitterBookingsViewModel @Inject constructor(
    private val repository: SitterBookingRepository
) : ViewModel() {

    private val _bookingsState = MutableLiveData<Resource<List<SitterBookingResponse>>>()
    val bookingsState: LiveData<Resource<List<SitterBookingResponse>>> = _bookingsState

    private var allBookings: List<SitterBookingResponse> = emptyList()
    var selectedDate: LocalDate? = null
    var selectedStatus: BookingStatus? = null

    fun loadSitterBookings(sitterId: String) {
        viewModelScope.launch {
            _bookingsState.value = Resource.Loading
            try {
                val response = repository.getSitterBookings(sitterId)
                if (response.success && response.data != null) {
                    allBookings = response.data
                    applyFilters()
                } else {
                    _bookingsState.value = Resource.Error(response.message ?: "載入失敗")
                }
            } catch (e: Exception) {
                _bookingsState.value = Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    fun loadPendingBookings(sitterId: String) {
        viewModelScope.launch {
            _bookingsState.value = Resource.Loading
            try {
                val response = repository.getPendingBookings(sitterId)
                if (response.success && response.data != null) {
                    allBookings = response.data
                    _bookingsState.value = Resource.Success(response.data)
                } else {
                    _bookingsState.value = Resource.Error(response.message ?: "載入失敗")
                }
            } catch (e: Exception) {
                _bookingsState.value = Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    fun applyFilters() {
        var filtered = allBookings

        // 日期篩選
        selectedDate?.let { date ->
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            filtered = filtered.filter { booking ->
                booking.startTime.startsWith(dateStr)
            }
        }

        // 狀態篩選
        selectedStatus?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        _bookingsState.value = Resource.Success(filtered)
    }

    fun clearFilters() {
        selectedDate = null
        selectedStatus = null
        _bookingsState.value = Resource.Success(allBookings)
    }
}
