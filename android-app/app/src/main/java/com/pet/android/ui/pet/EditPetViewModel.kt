package com.pet.android.ui.pet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.api.PetApi
import com.pet.android.data.model.*
import com.pet.android.data.repository.CatRepository
import com.pet.android.data.repository.DogRepository
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPetViewModel @Inject constructor(
    private val dogRepository: DogRepository,
    private val catRepository: CatRepository,
    private val petApi: PetApi
) : ViewModel() {

    // 寵物類型狀態
    private val _petTypeState = MutableLiveData<Resource<String>>()
    val petTypeState: LiveData<Resource<String>> = _petTypeState

    // 狗狗資料狀態
    private val _dogState = MutableLiveData<Resource<DogRequest>>()
    val dogState: LiveData<Resource<DogRequest>> = _dogState

    // 貓咪資料狀態
    private val _catState = MutableLiveData<Resource<CatRequest>>()
    val catState: LiveData<Resource<CatRequest>> = _catState

    // 更新狗狗狀態
    private val _updateDogState = MutableLiveData<Resource<DogRequest>>()
    val updateDogState: LiveData<Resource<DogRequest>> = _updateDogState

    // 更新貓咪狀態
    private val _updateCatState = MutableLiveData<Resource<CatRequest>>()
    val updateCatState: LiveData<Resource<CatRequest>> = _updateCatState

    /**
     * 載入寵物類型
     * 使用通用 PetApi 獲取正確的寵物類型，避免使用錯誤的 API 導致 404
     */
    fun loadPetType(id: String) {
        viewModelScope.launch {
            _petTypeState.value = Resource.Loading
            try {
                val response = petApi.getPetById(id)
                if (response.success && response.data != null) {
                    _petTypeState.value = Resource.Success(response.data.type)
                } else {
                    _petTypeState.value = Resource.Error(response.message ?: "獲取寵物類型失敗")
                }
            } catch (e: Exception) {
                _petTypeState.value = Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    /**
     * 載入狗狗詳細資料
     */
    fun loadDog(id: String) {
        viewModelScope.launch {
            _dogState.value = Resource.Loading
            _dogState.value = dogRepository.getDogById(id)
        }
    }

    /**
     * 載入貓咪詳細資料
     */
    fun loadCat(id: String) {
        viewModelScope.launch {
            _catState.value = Resource.Loading
            _catState.value = catRepository.getCatById(id)
        }
    }

    /**
     * 更新狗狗資料
     */
    fun updateDog(
        id: String,
        name: String,
        age: Int?,
        breed: String?,
        gender: Gender?,
        specialNeeds: String?,
        isNeutered: Boolean?,
        vaccineStatus: String?,
        size: DogSize?,
        isWalkRequired: Boolean?,
        walkFrequencyPerDay: Int?,
        trainingLevel: TrainingLevel?,
        isFriendlyWithDogs: Boolean?,
        isFriendlyWithPeople: Boolean?,
        isFriendlyWithChildren: Boolean?
    ) {
        viewModelScope.launch {
            _updateDogState.value = Resource.Loading
            val dog = DogRequest(
                id = id,
                name = name,
                age = age,
                breed = breed,
                gender = gender,
                specialNeeds = specialNeeds,
                isNeutered = isNeutered,
                vaccineStatus = vaccineStatus,
                size = size,
                isWalkRequired = isWalkRequired,
                walkFrequencyPerDay = walkFrequencyPerDay,
                trainingLevel = trainingLevel,
                isFriendlyWithDogs = isFriendlyWithDogs,
                isFriendlyWithPeople = isFriendlyWithPeople,
                isFriendlyWithChildren = isFriendlyWithChildren
            )
            _updateDogState.value = dogRepository.updateDog(id, dog)
        }
    }

    /**
     * 更新貓咪資料
     */
    fun updateCat(
        id: String,
        name: String,
        age: Int?,
        breed: String?,
        gender: Gender?,
        specialNeeds: String?,
        isNeutered: Boolean?,
        vaccineStatus: String?,
        isIndoor: Boolean?,
        litterBoxType: LitterBoxType?,
        scratchingHabit: ScratchingHabit?
    ) {
        viewModelScope.launch {
            _updateCatState.value = Resource.Loading
            val cat = CatRequest(
                id = id,
                name = name,
                age = age,
                breed = breed,
                gender = gender,
                specialNeeds = specialNeeds,
                isNeutered = isNeutered,
                vaccineStatus = vaccineStatus,
                isIndoor = isIndoor,
                litterBoxType = litterBoxType,
                scratchingHabit = scratchingHabit
            )
            _updateCatState.value = catRepository.updateCat(id, cat)
        }
    }
}
