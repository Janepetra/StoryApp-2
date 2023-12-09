package com.storyapp.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class SettingPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private val token = stringPreferencesKey("user_token")
    private val THEME_KEY = booleanPreferencesKey("theme_setting")

    fun getUserToken(): Flow<String> = dataStore.data.map { pref ->
        pref[token] ?: ""}

    suspend fun saveToken(userToken: String) {
        dataStore.edit { pref ->
            pref[token] = userToken
        }
    }

    suspend fun clearLogin() {
        dataStore.edit { pref ->
            pref.remove(token)
        }
    }

    fun getThemeSetting(): Flow<Boolean> {
        return dataStore.data.map { pref ->
            pref[THEME_KEY] ?: false
        }
    }

    suspend fun saveThemeSetting(isDarkModeActive: Boolean) {
        dataStore.edit { pref ->
            pref[THEME_KEY] = isDarkModeActive
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): SettingPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}