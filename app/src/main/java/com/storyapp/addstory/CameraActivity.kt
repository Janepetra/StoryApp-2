package com.storyapp.addstory

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.storyapp.R
import com.storyapp.customview.Helper
import com.storyapp.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var openGallery: ActivityResultLauncher<Intent>
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnAction()
        initGallery()
    }

    private fun btnAction() {
        binding.btnCamera.setOnClickListener {
            takePic()
        }
        binding.btnFlipcam.setOnClickListener {
            cameraSelector =
                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }
        binding.btnGallery.setOnClickListener {
            startGallery()
        }
        startCamera()
    }

    private fun startCamera() {
        //Membuat objek ProcessCameraProviderFuture untuk mendapatkan instance ProcessCameraProvider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            //Mendapatkan instance ProcessCameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    //Mengatur SurfaceProvider untuk menampilkan pratinjau pada SurfaceView
                    it.setSurfaceProvider(binding.pvCamera.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (exc: Exception) {
                Toast.makeText(
                    this@CameraActivity,
                    "Gagal memunculkan kamera.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "startCamera: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePic() {
        //check is file null or not
        val imageCapture = imageCapture ?: return
        //make object file untuk menyimpan photo yg diambil
        val photoFile = Helper.createFile(application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val intent = Intent(this@CameraActivity, AddStoryActivity::class.java)
                    intent.putExtra(AddStoryActivity.EXTRA_CAMERAX_IMAGE, photoFile)
                    intent.putExtra(
                        AddStoryActivity.EXTRA_CAMERAX_MODE, cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
                    )
                    this@CameraActivity.finish()
                    startActivity(intent)
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.failed_to_get_photo),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "onError: ${exc.message}")
                }
            }
        )
    }

    private fun initGallery() {
        openGallery = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                //get Uri dari data
                val selectedImg: Uri = result.data?.data as Uri
                //convert uri diatas menggunakan fun uriToFile
                val myFile = Helper.uriToFile(selectedImg, this@CameraActivity)
                //send data in putExtra
                val intent = Intent(this@CameraActivity, AddStoryActivity::class.java)
                intent.putExtra(AddStoryActivity.EXTRA_CAMERAX_IMAGE, myFile)
                intent.putExtra(
                    AddStoryActivity.EXTRA_CAMERAX_MODE,
                    cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
                )
                this@CameraActivity.finish()
                startActivity(intent)
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        //menetukan tipe data yaitu image
        intent.type = "image/*"
        //Membuat Intent chooser untuk menampilkan aplikasi yang dapat menangani tipe data
        val chooser = Intent.createChooser(intent, "Pick a Photo")
        openGallery.launch(chooser)
    }

    companion object {
        private const val TAG = "CameraActivity"
    }
}