package com.pet.android.data.repository

import com.pet.android.data.api.PetActivityApi
import com.pet.android.data.model.PetActivityResponse
import com.pet.android.data.model.RecordActivityRequest
import com.pet.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetActivityRepository @Inject constructor(
    private val petActivityApi: PetActivityApi
) {
    suspend fun getTodayActivity(petId: String): Resource<PetActivityResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = petActivityApi.getTodayActivity(petId)
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "取得今日活動失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    suspend fun recordActivity(petId: String, request: RecordActivityRequest): Resource<PetActivityResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = petActivityApi.recordActivity(petId, request)
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "記錄活動失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    suspend fun getActivityHistory(petId: String): Resource<List<PetActivityResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = petActivityApi.getActivityHistory(petId)
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "取得活動歷史失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }
}
