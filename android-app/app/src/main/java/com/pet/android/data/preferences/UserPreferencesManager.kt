package com.pet.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pet.android.data.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val LOGIN_DATE_KEY = longPreferencesKey("login_date")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val ROLE_NAME_KEY = stringPreferencesKey("role_name")
    }

    val username: Flow<String?> = context.userDataStore.data
        .map { preferences -> preferences[USERNAME_KEY] }

    val loginDate: Flow<Long?> = context.userDataStore.data
        .map { preferences -> preferences[LOGIN_DATE_KEY] }

    val userRole: Flow<String?> = context.userDataStore.data
        .map { preferences -> preferences[USER_ROLE_KEY] }

    val userId: Flow<String?> = context.userDataStore.data
        .map { preferences -> preferences[USER_ID_KEY] }

    val roleName: Flow<String?> = context.userDataStore.data
        .map { preferences -> preferences[ROLE_NAME_KEY] }

    // Convenience: access role as enum while keeping stored string compatibility
    val userRoleEnum: Flow<UserRole> = userRole.map { value ->
        UserRole.fromString(value)
    }

    suspend fun saveLoginData(username: String, role: String) {
        context.userDataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[LOGIN_DATE_KEY] = System.currentTimeMillis()
            preferences[USER_ROLE_KEY] = role
        }
    }

    // Overload: save with enum
    suspend fun saveLoginData(username: String, role: UserRole) {
        saveLoginData(username, role.code)
    }

    // Overload: save with userId as well
    suspend fun saveLoginData(username: String, role: String, userId: String) {
        context.userDataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[LOGIN_DATE_KEY] = System.currentTimeMillis()
            preferences[USER_ROLE_KEY] = role
            preferences[USER_ID_KEY] = userId
        }
    }

    // Overload: save with userId and roleName
    suspend fun saveLoginData(username: String, role: String, userId: String, roleName: String?) {
        context.userDataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[LOGIN_DATE_KEY] = System.currentTimeMillis()
            preferences[USER_ROLE_KEY] = role
            preferences[USER_ID_KEY] = userId
            if (roleName != null) {
                preferences[ROLE_NAME_KEY] = roleName
            }
        }
    }

    // Overload: save with enum role and userId
    suspend fun saveLoginData(username: String, role: UserRole, userId: String) {
        saveLoginData(username, role.code, userId)
    }

    // Overload: save with enum role, userId and roleName
    suspend fun saveLoginData(username: String, role: UserRole, userId: String, roleName: String?) {
        saveLoginData(username, role.code, userId, roleName)
    }

    suspend fun clearLoginData() {
        context.userDataStore.edit { preferences ->
            preferences.remove(USERNAME_KEY)
            preferences.remove(LOGIN_DATE_KEY)
            preferences.remove(USER_ROLE_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(ROLE_NAME_KEY)
        }
    }
}
