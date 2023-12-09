package com.storyapp.authentication

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.storyapp.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        playAnimation()
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginForm::class.java))
        }

        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, SignUpForm::class.java))
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageWelcome, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val titleWelcome = ObjectAnimator.ofFloat(binding.titleWelcome, View.ALPHA, 1f).setDuration(200)
        val textWelcome = ObjectAnimator.ofFloat(binding.textWelcome, View.ALPHA, 1f).setDuration(200)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(200)
        val btnSignup = ObjectAnimator.ofFloat(binding.btnSignup, View.ALPHA, 1f).setDuration(200)

        AnimatorSet().apply {
            playSequentially(
                titleWelcome,
                textWelcome,
                btnLogin,
                btnSignup)
            start()
        }
    }
}