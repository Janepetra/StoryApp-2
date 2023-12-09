package com.storyapp.authentication

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.storyapp.customview.Helper
import com.storyapp.dashboard.MainActivity
import com.storyapp.databinding.ActivityLoginFormBinding
import com.storyapp.viewmodel.DetailStoryViewModel
import com.storyapp.viewmodel.SettingModelFactory
import com.storyapp.viewmodel.SettingPreferences
import com.storyapp.viewmodel.SettingViewModel
import com.storyapp.viewmodel.ViewModelFactory
import com.storyapp.viewmodel.dataStore

class LoginForm : AppCompatActivity() {
    private lateinit var binding: ActivityLoginFormBinding
    private val viewModel: DetailStoryViewModel by viewModels {
        ViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getToken()
        checkData()
        showLoading()
        setupView()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getToken() {
        val pref = SettingPreferences.getInstance(dataStore)
        val settingViewModel = ViewModelProvider(this, SettingModelFactory(pref))[SettingViewModel::class.java]

        //get token and put in preferences
        viewModel.let { model ->
            model.login.observe(this) { login ->
                settingViewModel.setUserToken(
                    login.loginResult.token
                )
            }
        }
        settingViewModel.getUserTokens().observe(this) { token ->
            if (token != "")
                AlertDialog.Builder(this).apply {
                    setTitle("Yeah!")
                    setMessage("Your login succeed, can't wait to read your inspiring story")
                    setPositiveButton("Next") { _, _ ->
                        val intent = Intent(this@LoginForm, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    create()
                    show()
                }
        }
    }

    private fun checkData() {
        binding.btnLoginForm.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            when {
                email.isEmpty() or password.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Please filled Email and Password first",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                !email.matches(Helper.pattern) -> {
                    Toast.makeText(this, "Email format is invalid",
                        Toast.LENGTH_SHORT).show()
                }
                password.length < 8 -> {
                    Toast.makeText(
                        this,
                        "Password must cointain at least 8 characters",
                        Toast.LENGTH_SHORT).show()
                }
                else -> {
                    //get login data
                    viewModel.getLogin(email, password)
                }
            }
        }
    }

    private fun showLoading() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageLogin, View.TRANSLATION_X, -50f, 50f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val titleLogin = ObjectAnimator.ofFloat(binding.titleLogin, View.ALPHA, 1f).setDuration(200)
        val textLogin = ObjectAnimator.ofFloat(binding.textLogin, View.ALPHA, 1f).setDuration(200)
        val emailLogin = ObjectAnimator.ofFloat(binding.emailLogin, View.ALPHA, 1f).setDuration(200)
        val emailEditText = ObjectAnimator.ofFloat(binding.emailEditText, View.ALPHA, 1f).setDuration(200)
        val passwordLogin = ObjectAnimator.ofFloat(binding.passwordLogin, View.ALPHA, 1f).setDuration(200)
        val passwordEditText = ObjectAnimator.ofFloat(binding.passwordEditText, View.ALPHA, 1f).setDuration(200)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLoginForm, View.ALPHA, 1f).setDuration(200)

        AnimatorSet().apply {
            playSequentially(
                titleLogin,
                textLogin,
                emailLogin,
                emailEditText,
                passwordLogin,
                passwordEditText,
                btnLogin)
            start()
        }
    }
}