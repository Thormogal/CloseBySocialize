package com.example.closebysocialize.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.example.closebysocialize.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        // Initialize the GoogleMap asynchronously
        mapView.getMapAsync(this)

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        mapView?.let { mapView ->
            // Check if location permission is granted
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request location permission if not granted
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                return
            }

            // Permission is granted, enable location layer
            googleMap.isMyLocationEnabled = true

            // Get last known location and move camera to it
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting last known location", e)
                }
        } ?: Log.e(TAG, "MapView is null")
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 15f
        private const val TAG = "MapFragment"
    }


//        val sweden = LatLng(60.4843, 15.4340)
//        val cameraPosition = CameraPosition.Builder()
//            .target(sweden) // Sets the center of the map to Sweden
//            .zoom(6f) // Sets the zoom level
//            .build()
//
//        // Move and animate the camera to the specified position
//        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}


