package com.storyapp.authentication

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import com.storyapp.R
import com.storyapp.customview.Helper
import com.storyapp.databinding.ActivitySignUpFormBinding
import com.storyapp.viewmodel.DetailStoryViewModel
import com.storyapp.viewmodel.ViewModelFactory

class SignUpForm : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpFormBinding
    private val viewModel: DetailStoryViewModel by viewModels {
        ViewModelFactory.getInstance(application)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        checkData()
        showLoading()
        playAnimation()
        goToLogin()
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

    private fun checkData() {
        binding.btnSignupForm.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            when {
                email.isEmpty() or password.isEmpty() -> {
                    Toast.makeText(this,
                        getString(R.string.email_and_password_isBlank), Toast.LENGTH_SHORT).show()
                }
                !email.matches(Helper.pattern) -> {
                    Toast.makeText(this,
                        getString(R.string.email_format_is_invalid), Toast.LENGTH_SHORT).show()
                }
                password.length < 8 -> {
                    Toast.makeText(this, getString(R.string.password_error), Toast.LENGTH_SHORT).show()
                } else -> {
                viewModel.getSignup(name, email, password)
                }
            }
        }
    }

    private fun goToLogin() {
        viewModel.signup.observe(this) { signup ->
            if (signup.error != true) {
                AlertDialog.Builder(this).apply {
                    setTitle("Yeah!")
                    setMessage(getString(R.string.login_success))
                    setPositiveButton("Next") { _, _ ->
                        val intent = Intent(this@SignUpForm, LoginForm::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    create()
                    show()
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
        ObjectAnimator.ofFloat(binding.imageSignup, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val titleSignup = ObjectAnimator.ofFloat(binding.titleSignup, View.ALPHA, 1f).setDuration(200)
        val nameSignup = ObjectAnimator.ofFloat(binding.nameSignup, View.ALPHA, 1f).setDuration(200)
        val nameEditText = ObjectAnimator.ofFloat(binding.nameEditText, View.ALPHA, 1f).setDuration(200)
        val emailSignup = ObjectAnimator.ofFloat(binding.emailSignup, View.ALPHA, 1f).setDuration(200)
        val emailEditText = ObjectAnimator.ofFloat(binding.emailEditText, View.ALPHA, 1f).setDuration(200)
        val passwordSignup = ObjectAnimator.ofFloat(binding.passwordSignup, View.ALPHA, 1f).setDuration(200)
        val passwordEditText = ObjectAnimator.ofFloat(binding.passwordEditText, View.ALPHA, 1f).setDuration(200)
        val btnSignup = ObjectAnimator.ofFloat(binding.btnSignupForm, View.ALPHA, 1f).setDuration(200)

        AnimatorSet().apply {
            playSequentially(
                titleSignup,
                nameSignup,
                nameEditText,
                emailSignup,
                emailEditText,
                passwordSignup,
                passwordEditText,
                btnSignup)
            start()
        }
    }
}