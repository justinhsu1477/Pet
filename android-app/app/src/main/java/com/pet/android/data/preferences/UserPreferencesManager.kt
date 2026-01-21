package com.pet.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
    }

    val username: Flow<String?> = context.userDataStore.data
        .map { preferences -> preferences[USERNAME_KEY] }

    val loginDate: Flow<Long?> = context.userDataStore.data
        .map { preferences -> preferences[LOGIN_DATE_KEY] }

    val userRole: Flow<String?> = context.userDataStore.data
        .map { preferences -> preferences[USER_ROLE_KEY] }

    suspend fun saveLoginData(username: String, role: String) {
        context.userDataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[LOGIN_DATE_KEY] = System.currentTimeMillis()
            preferences[USER_ROLE_KEY] = role
        }
    }

    suspend fun clearLoginData() {
        context.userDataStore.edit { preferences ->
            preferences.remove(USERNAME_KEY)
            preferences.remove(LOGIN_DATE_KEY)
            preferences.remove(USER_ROLE_KEY)
        }
    }
}
