package com.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.storyapp.viewmodel.SettingPreferences
import kotlinx.coroutines.launch

class SettingViewModel(private val pref: SettingPreferences): ViewModel() {

    fun getUserTokens(): LiveData<String> {
        return pref.getUserToken().asLiveData()
    }

    fun setUserToken(userToken: String) {
        viewModelScope.launch {
            pref.saveToken(userToken)
        }
    }

    fun getThemeSettings(): LiveData<Boolean> {
        return pref.getThemeSetting().asLiveData()
    }

    fun saveThemeSetting(isDarkModeActive: Boolean) {
        viewModelScope.launch {
            pref.saveThemeSetting(isDarkModeActive)
        }
    }

    fun clearUserData() {
        viewModelScope.launch {
            pref.clearLogin()
        }
    }
}