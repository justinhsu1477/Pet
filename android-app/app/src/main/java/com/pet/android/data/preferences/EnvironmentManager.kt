package com.pet.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class EnvironmentManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ENVIRONMENT_KEY = stringPreferencesKey("current_environment")
    }

    enum class Environment(val displayName: String, val baseUrl: String) {
        // 172.20.10.2 = iPhone 熱點下電腦的固定 IP
        // 10.0.2.2 = Android 模擬器存取 localhost 的特殊 IP
        DEVELOPMENT("開發環境", "http://172.20.10.2:8080/"),
        STAGING("測試環境", "https://staging-api.petcare.com/"),
        PRODUCTION("正式環境", "https://api.petcare.com/")
    }

    val currentEnvironment: Flow<Environment> = context.dataStore.data
        .map { preferences ->
            val envName = preferences[ENVIRONMENT_KEY] ?: Environment.DEVELOPMENT.name
            try {
                Environment.valueOf(envName)
            } catch (e: IllegalArgumentException) {
                Environment.DEVELOPMENT
            }
        }

    suspend fun setEnvironment(environment: Environment) {
        context.dataStore.edit { preferences ->
            preferences[ENVIRONMENT_KEY] = environment.name
        }
    }

    suspend fun getCurrentEnvironment(): Environment {
        val preferences = context.dataStore.data.map { it[ENVIRONMENT_KEY] }
        val envName = preferences.map { it ?: Environment.DEVELOPMENT.name }
        return try {
            Environment.valueOf(envName.toString())
        } catch (e: IllegalArgumentException) {
            Environment.DEVELOPMENT
        }
    }
}
