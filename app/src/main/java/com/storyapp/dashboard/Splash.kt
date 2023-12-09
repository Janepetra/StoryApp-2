package com.storyapp.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.storyapp.R
import com.storyapp.authentication.WelcomeActivity
import com.storyapp.viewmodel.SettingModelFactory
import com.storyapp.viewmodel.SettingPreferences
import com.storyapp.viewmodel.SettingViewModel
import com.storyapp.viewmodel.dataStore
import java.util.Timer
import kotlin.concurrent.schedule

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        val pref = SettingPreferences.getInstance(dataStore)
        val loginViewModel = ViewModelProvider(this, SettingModelFactory(pref))[SettingViewModel::class.java]

        loginViewModel.getUserTokens().observe(this) { token ->
                if (token == "") Timer().schedule(2000) {
                    startActivity(Intent(this@Splash, WelcomeActivity::class.java))
                    finish()
                } else Timer().schedule(2000) {
                    startActivity(Intent(this@Splash, MainActivity::class.java))
                    finish()
                }
            }
    }
}