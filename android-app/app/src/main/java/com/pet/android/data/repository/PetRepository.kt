package com.pet.android.data.repository

import com.pet.android.data.api.PetApi
import com.pet.android.data.model.Pet
import com.pet.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepository @Inject constructor(
    private val petApi: PetApi
) {
    suspend fun getAllPets(): Resource<List<Pet>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = petApi.getAllPets()
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "取得寵物列表失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    suspend fun createPet(pet: Pet): Resource<Pet> {
        return withContext(Dispatchers.IO) {
            try {
                val response = petApi.createPet(pet)
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "新增寵物失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    suspend fun deletePet(id: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = petApi.deletePet(id)
                if (response.success) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.message ?: "刪除寵物失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }
}
