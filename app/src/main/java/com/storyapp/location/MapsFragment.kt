package com.storyapp.location

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.storyapp.R
import com.storyapp.dashboard.MainActivity
import com.storyapp.databinding.FragmentMapsBinding
import com.storyapp.db.local.entity.Story
import com.storyapp.viewmodel.DetailStoryViewModel
import com.storyapp.viewmodel.SettingModelFactory
import com.storyapp.viewmodel.SettingPreferences
import com.storyapp.viewmodel.SettingViewModel
import com.storyapp.viewmodel.ViewModelFactory
import com.storyapp.viewmodel.dataStore

class MapsFragment : Fragment() {

    private lateinit var binding: FragmentMapsBinding
    private lateinit var mMap: GoogleMap
    private val boundBuilder = LatLngBounds.Builder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var token: String = ""
    private var locViewModel: DetailStoryViewModel? = null
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        // saya gatau kak knp tidak menampilkan maps padahal udh di inisiasi
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        //get token from preferences
        locViewModel = ViewModelProvider(this, ViewModelFactory(activity as MainActivity))[DetailStoryViewModel::class.java]
        val pref = SettingPreferences.getInstance((activity as MainActivity).dataStore)
        val settingViewModel = ViewModelProvider(this, SettingModelFactory(pref))[SettingViewModel::class.java]

        settingViewModel.getUserTokens().observe(viewLifecycleOwner) {
            token = StringBuilder("Bearer ").append(it).toString()
            locViewModel?.getListStory(token)
        }

        locViewModel!!.listStory.observe(viewLifecycleOwner) {
            if(it != null) {
                setMarker(it)
            }
        }
        showLoading()
    }

    private fun getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8f))
                } else {
                    Toast.makeText(requireContext(), "Please activate your location", Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private fun setMarker(data: List<Story>) {
        lateinit var locationZoom: LatLng
        data.forEach {
            if (it.lat != null && it.lon != null) {
                val latLng = LatLng(it.lat, it.lon)
                val address = LocationConverter.getStringAddress(latLng, (activity as MainActivity))
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(it.name)
                        .snippet(address)
                )
                boundBuilder.include(latLng)
                marker?.tag = it

                locationZoom = latLng
            }
        }

        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                locationZoom, 3f
            )
        )
    }

    private fun setMapStyle(mapsType: String) {
        if (mapsType == "standard") {
            try {
                val success =
                    mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            requireContext(),
                            R.raw.maps_standard
                        )
                    )
                if (!success) {
                    Log.e(ContentValues.TAG, "Style parsing failed.")
                }
            } catch (exception: Resources.NotFoundException) {
                Log.e(ContentValues.TAG, "Can't find style. Error: ", exception)
            }
        } else {
            try {
                val success =
                    mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            requireContext(),
                            R.raw.map_style
                        )
                    )
                if (!success) {
                    Log.e(ContentValues.TAG, "Style parsing failed.")
                }
            } catch (exception: Resources.NotFoundException) {
                Log.e(ContentValues.TAG, "Can't find style. Error: ", exception)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.maps_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.map_standar -> {
                setMapStyle("standard")
                true
            }
            R.id.map_night -> {
                setMapStyle("night")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                getDeviceLocation()
            }
        }

    private fun showLoading() {
        locViewModel?.isLoading?.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}