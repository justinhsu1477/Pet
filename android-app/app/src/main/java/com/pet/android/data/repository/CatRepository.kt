package com.pet.android.data.repository

import com.pet.android.data.api.CatApi
import com.pet.android.data.model.CatRequest
import com.pet.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatRepository @Inject constructor(
    private val catApi: CatApi
) {
    suspend fun getAllCats(): Resource<List<CatRequest>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = catApi.getAllCats()
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "取得貓咪列表失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    suspend fun createCat(cat: CatRequest): Resource<CatRequest> {
        return withContext(Dispatchers.IO) {
            try {
                val response = catApi.createCat(cat)
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "新增貓咪失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    suspend fun deleteCat(id: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = catApi.deleteCat(id)
                if (response.success) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.message ?: "刪除貓咪失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }
}
