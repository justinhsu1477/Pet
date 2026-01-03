package com.pet.android.data.repository

import com.pet.android.data.api.SitterApi
import com.pet.android.data.model.Sitter
import com.pet.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SitterRepository @Inject constructor(
    private val sitterApi: SitterApi
) {
    suspend fun getAllSitters(): Resource<List<Sitter>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = sitterApi.getAllSitters()
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "取得保母列表失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    suspend fun createSitter(sitter: Sitter): Resource<Sitter> {
        return withContext(Dispatchers.IO) {
            try {
                val response = sitterApi.createSitter(sitter)
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "新增保母失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }
}
