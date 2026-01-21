package com.pet.android.ui.sitter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.model.Sitter
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.data.repository.SitterRepository
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SitterViewModel @Inject constructor(
    private val sitterRepository: SitterRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val permissionService: com.pet.android.data.service.PermissionService
) : ViewModel() {

    val userRole: Flow<String?> = userPreferencesManager.userRole

    private val _sittersState = MutableLiveData<Resource<List<Sitter>>>()
    val sittersState: LiveData<Resource<List<Sitter>>> = _sittersState

    fun loadSitters() {
        viewModelScope.launch {
            _sittersState.value = Resource.Loading
            _sittersState.value = sitterRepository.getAllSitters()
        }
    }

    suspend fun isAdmin(): Boolean = permissionService.isAdmin()
}
