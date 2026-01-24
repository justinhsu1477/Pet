package com.pet.android.ui.sitter.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 保母個人檔案 ViewModel
 *
 * 注意: 目前使用模擬資料,實際應該從後端 API 獲取和保存
 */
@HiltViewModel
class SitterProfileViewModel @Inject constructor(
    // TODO: 注入 SitterRepository 當後端 API 準備好時
    // private val sitterRepository: SitterRepository
) : ViewModel() {

    private val _profileState = MutableLiveData<Resource<SitterProfileData>>()
    val profileState: LiveData<Resource<SitterProfileData>> = _profileState

    private val _saveState = MutableLiveData<Resource<Unit>>()
    val saveState: LiveData<Resource<Unit>> = _saveState

    /**
     * 載入保母個人檔案
     */
    fun loadProfile(sitterId: String) {
        viewModelScope.launch {
            _profileState.value = Resource.Loading

            try {
                // TODO: 實際應該從後端 API 獲取
                // val response = sitterRepository.getSitterProfile(sitterId)

                // 暫時使用模擬資料
                delay(500) // 模擬網路延遲

                val mockProfile = SitterProfileData(
                    introduction = "我是一位熱愛寵物的專業保母,有5年以上的照顧經驗。",
                    experience = "曾在寵物美容店工作3年,並有CPDT-KA認證訓練師資格。",
                    serviceArea = "台北市、新北市",
                    pricing = "遛狗: NT$400/小時\n餵食照顧: NT$600/天\n寵物訓練: NT$800/小時",
                    availableTime = "週一至週五 14:00-20:00\n週末 09:00-18:00",
                    certifications = "CPDT-KA 認證訓練師\n寵物美容C級證照",
                    services = listOf("遛狗", "餵食", "陪玩")
                )

                _profileState.value = Resource.Success(mockProfile)
            } catch (e: Exception) {
                _profileState.value = Resource.Error(e.message ?: "載入個人檔案失敗")
            }
        }
    }

    /**
     * 保存保母個人檔案
     */
    fun saveProfile(sitterId: String, profileData: SitterProfileData) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading

            try {
                // TODO: 實際應該呼叫後端 API 保存
                // val response = sitterRepository.updateSitterProfile(sitterId, profileData)

                // 暫時模擬儲存成功
                delay(800) // 模擬網路延遲

                _saveState.value = Resource.Success(Unit)

                // 更新 profileState 以反映最新資料
                _profileState.value = Resource.Success(profileData)
            } catch (e: Exception) {
                _saveState.value = Resource.Error(e.message ?: "儲存個人檔案失敗")
            }
        }
    }
}
