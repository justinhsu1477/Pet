package com.pet.android.ui.sitter.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.api.SitterBookingApi
import com.pet.android.data.model.BookingStatisticsResponse
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SitterStatisticsViewModel @Inject constructor(
    private val sitterBookingApi: SitterBookingApi
) : ViewModel() {

    private val _statisticsState = MutableLiveData<Resource<BookingStatisticsResponse>>()
    val statisticsState: LiveData<Resource<BookingStatisticsResponse>> = _statisticsState

    /**
     * 載入統計資料
     */
    fun loadStatistics(sitterId: String) {
        viewModelScope.launch {
            _statisticsState.value = Resource.Loading

            try {
                val response = sitterBookingApi.getStatistics(sitterId)

                if (response.success && response.data != null) {
                    _statisticsState.value = Resource.Success(response.data)
                } else {
                    _statisticsState.value = Resource.Error(
                        response.message ?: "無法取得統計資料"
                    )
                }
            } catch (e: Exception) {
                _statisticsState.value = Resource.Error(
                    e.message ?: "網路錯誤，請稍後再試"
                )
            }
        }
    }
}
