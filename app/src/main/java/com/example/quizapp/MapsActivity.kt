package com.example.quizapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.quizapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Convenient root view for Snackbars
    private val root: View get() = findViewById(android.R.id.content)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Make sure the SupportMapFragment is hooked up
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // (Optional) tweak UI
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        enableMyLocation()
    }

    private fun enableMyLocation() {
        val fineGranted = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Turn on the blue dot — guard SecurityException
        try {
            mMap.isMyLocationEnabled = true
        } catch (t: SecurityException) {
            root.showError(t)
            return
        }

        // Try to place the camera on the last known location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val here = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(here).title("You are here"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 16f))
                } else {
                    Snackbar.make(root, "Couldn’t get your location yet. Try again in a moment.", Snackbar.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { t -> root.showError(t) } // small, friendly error handling
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        ) {
            enableMyLocation()
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            root.showError(SecurityException("Location permission is required."))
        }
    }
}

/** Tiny, local helper for friendly errors — lives in THIS file (no new files). */
private fun View.showError(t: Throwable) {
    val msg = when (t) {
        is java.net.UnknownHostException -> "No internet connection."
        is java.net.SocketTimeoutException -> "Request timed out."
        is SecurityException -> "Location permission is required."
        else -> t.message ?: "Something went wrong."
    }
    Snackbar.make(this, msg, Snackbar.LENGTH_LONG).show()
}
