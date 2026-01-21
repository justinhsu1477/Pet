package com.pet.android.data.service

import com.pet.android.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionService @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) {
    companion object {
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_USER = "USER"
        const val ROLE_SITTER = "SITTER"
    }

    /**
     * Check if current user is admin (suspend function)
     */
    suspend fun isAdmin(): Boolean {
        val role = userPreferencesManager.userRole.first()
        return role == ROLE_ADMIN
    }

    /**
     * Check if current user is sitter (suspend function)
     */
    suspend fun isSitter(): Boolean {
        val role = userPreferencesManager.userRole.first()
        return role == ROLE_SITTER
    }

    /**
     * Check if current user is regular user (suspend function)
     */
    suspend fun isUser(): Boolean {
        val role = userPreferencesManager.userRole.first()
        return role == ROLE_USER
    }

    /**
     * Get current user role as Flow (reactive)
     */
    val currentRole: Flow<String?> = userPreferencesManager.userRole

    /**
     * Check if current user is admin (reactive Flow)
     */
    val isAdminFlow: Flow<Boolean> = userPreferencesManager.userRole.map { role ->
        role == ROLE_ADMIN
    }

    /**
     * Check if current user is sitter (reactive Flow)
     */
    val isSitterFlow: Flow<Boolean> = userPreferencesManager.userRole.map { role ->
        role == ROLE_SITTER
    }

    /**
     * Check if current user is regular user (reactive Flow)
     */
    val isUserFlow: Flow<Boolean> = userPreferencesManager.userRole.map { role ->
        role == ROLE_USER
    }

    /**
     * Check if user has specific role
     */
    suspend fun hasRole(role: String): Boolean {
        val currentRole = userPreferencesManager.userRole.first()
        return currentRole == role
    }
}
