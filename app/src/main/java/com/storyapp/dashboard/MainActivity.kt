package com.storyapp.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.storyapp.R
import com.storyapp.addstory.CameraActivity
import com.storyapp.databinding.ActivityMainBinding
import com.storyapp.location.MapsFragment

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val fragmentHome = HomeFragment()
    private val fragmentSettings = SettingsFragment()
    private val fragmentMaps = MapsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNav()
        switchNav(fragmentHome)
    }

    private fun switchNav(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun bottomNav() {
        binding.bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> switchNav(fragmentHome)
                R.id.nav_settings -> switchNav(fragmentSettings)
                R.id.nav_add -> directToCam()
                R.id.nav_loc -> switchNav(fragmentMaps)
            }
            true
        }
    }

    private fun directToCam() {
        if (!allPermissionsGranted()) {
            reqPermission()
        } else {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }

    private fun reqPermission() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }

    private fun allPermissionsGranted() =
        REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}