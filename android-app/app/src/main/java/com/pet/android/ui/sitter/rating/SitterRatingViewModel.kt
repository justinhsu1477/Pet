package com.pet.android.ui.sitter.rating

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.model.SitterRatingResponse
import com.pet.android.data.model.SitterRatingStatsResponse
import com.pet.android.data.repository.SitterRatingRepository
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SitterRatingViewModel @Inject constructor(
    private val repo: SitterRatingRepository
) : ViewModel() {

    private val _listState = MutableLiveData<Resource<List<SitterRatingResponse>>>()
    val listState: LiveData<Resource<List<SitterRatingResponse>>> = _listState

    private val _statsState = MutableLiveData<Resource<SitterRatingStatsResponse>>()
    val statsState: LiveData<Resource<SitterRatingStatsResponse>> = _statsState

    fun load(sitterId: String, page: Int = 0, size: Int = 10) {
        loadStats(sitterId)
        loadList(sitterId, page, size)
    }

    private fun loadList(sitterId: String, page: Int, size: Int) {
        _listState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val resp = repo.getRatings(sitterId, page, size)
                val data = resp.data?.content ?: emptyList()
                _listState.value = Resource.Success(data)
            } catch (e: Exception) {
                _listState.value = Resource.Error(e.message ?: "載入評價失敗")
            }
        }
    }

    private fun loadStats(sitterId: String) {
        _statsState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val resp = repo.getStats(sitterId)
                resp.data?.let { _statsState.value = Resource.Success(it) }
                    ?: run { _statsState.value = Resource.Error("載入統計失敗") }
            } catch (e: Exception) {
                _statsState.value = Resource.Error(e.message ?: "載入統計失敗")
            }
        }
    }
}