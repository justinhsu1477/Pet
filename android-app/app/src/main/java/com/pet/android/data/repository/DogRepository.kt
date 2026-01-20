package com.pet.android.data.repository

import com.pet.android.data.api.DogApi
import com.pet.android.data.model.DogRequest
import com.pet.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DogRepository @Inject constructor(
    private val dogApi: DogApi
) {
    suspend fun getAllDogs(): Resource<List<DogRequest>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = dogApi.getAllDogs()
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "取得狗狗列表失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    suspend fun createDog(dog: DogRequest): Resource<DogRequest> {
        return withContext(Dispatchers.IO) {
            try {
                val response = dogApi.createDog(dog)
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "新增狗狗失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    suspend fun deleteDog(id: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = dogApi.deleteDog(id)
                if (response.success) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.message ?: "刪除狗狗失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }
}
