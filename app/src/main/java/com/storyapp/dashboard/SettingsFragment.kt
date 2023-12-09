package com.storyapp.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.storyapp.authentication.WelcomeActivity
import com.storyapp.databinding.FragmentSettingsBinding
import com.storyapp.viewmodel.SettingModelFactory
import com.storyapp.viewmodel.SettingPreferences
import com.storyapp.viewmodel.SettingViewModel
import com.storyapp.viewmodel.dataStore

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var Token: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref = SettingPreferences.getInstance((activity as MainActivity).dataStore)
        val settingViewModel = ViewModelProvider(this, SettingModelFactory(pref))[SettingViewModel::class.java]

        settingViewModel.getUserTokens().observe(viewLifecycleOwner) {
            Token = StringBuilder("Bearer ").append(it).toString()
        }

        binding.btnLogout.setOnClickListener {
            settingViewModel.clearUserData()
            val intent = Intent(context, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        binding.btnDarkMode.setOnClickListener {
            settingViewModel.getThemeSettings().observe(viewLifecycleOwner) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            settingViewModel.saveThemeSetting(true)
        }
        binding.btnLightMode.setOnClickListener {
            settingViewModel.getThemeSettings().observe(viewLifecycleOwner) { isDarkModeActive: Boolean ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            settingViewModel.saveThemeSetting(false)
        }
    }
}