package com.storyapp.addstory

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.storyapp.R
import com.storyapp.customview.Helper
import com.storyapp.dashboard.MainActivity
import com.storyapp.databinding.ActivityAddStoryBinding
import com.storyapp.location.ChooseLocationActivity
import com.storyapp.viewmodel.DetailStoryViewModel
import com.storyapp.viewmodel.SettingModelFactory
import com.storyapp.viewmodel.SettingPreferences
import com.storyapp.viewmodel.SettingViewModel
import com.storyapp.viewmodel.ViewModelFactory
import com.storyapp.viewmodel.dataStore
import java.io.File

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var token: String? = null
    private var storyViewModel: DetailStoryViewModel? = null
    private var latlon: LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getToken()
        getPhoto()

        //tidak bisa berpindah ke halaman choose location
        binding.btnAddLoc.setOnClickListener {
            val intent = Intent(this, ChooseLocationActivity::class.java)
            //startActivity(intent)
            resultLauncher.launch(intent)
        }
    }

    private fun getToken() {
        val pref = SettingPreferences.getInstance(dataStore)
        val settingViewModel = ViewModelProvider(this, SettingModelFactory(pref))[SettingViewModel::class.java]
        storyViewModel = ViewModelProvider(this, ViewModelFactory(application))[DetailStoryViewModel::class.java]

        settingViewModel.getUserTokens().observe(this) {
            token = StringBuilder("Bearer ").append(it).toString()
        }
    }

    // Define an ActivityResultLauncher with StartActivityForResult contract
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // If the result is OK, extract data from the intent
                result.data?.let { data ->
                    val address = data.getStringExtra("address")
                    val lat = data.getDoubleExtra("lat", 0.0)
                    val lon = data.getDoubleExtra("lon", 0.0)
                    latlon = LatLng(lat, lon)

                    binding.detailLocation.text = address
                }
            }
        }

    private fun getPhoto() {
        val myFile = intent?.getSerializableExtra(EXTRA_CAMERAX_IMAGE) as File
        val isFromBackCamera = intent?.getBooleanExtra(EXTRA_CAMERAX_MODE, true) as Boolean
        val resultBitmap = Helper.rotateBitmap(
            BitmapFactory.decodeFile(myFile.path),
            isFromBackCamera
        )

        binding.imgAddStory.setImageBitmap(resultBitmap)
        binding.btnAddStory.setOnClickListener {
            if(binding.descAddStory.text.isNotEmpty()) {
                uploadPic(myFile, binding.descAddStory.text.toString())
            } else {
                Toast.makeText(this,
                    getString(R.string.please_filled_story_description), Toast.LENGTH_SHORT).show()
            }
        }
        storyViewModel?.let {
            it.addStory.observe(this) {
                if(true) {
                    Toast.makeText(this, getString(R.string.selected_photo), Toast.LENGTH_SHORT).show()
                }
            }
            it.isLoading.observe(this) {isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }
    //upload story, kadang bisa kadang engga
    private fun uploadPic(image: File, description: String) {
        if (token != null) {
            storyViewModel?.addStory(token!!, image, description)
            AlertDialog.Builder(this).apply {
                setTitle("Yeah!")
                setMessage(getString(R.string.success_message))
                val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        } else {
            Toast.makeText(this, getString(R.string.unauthorized), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        finish()
        return super.onSupportNavigateUp()
    }

    companion object {
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
        const val EXTRA_CAMERAX_MODE = "CameraX_Mode"
    }
}