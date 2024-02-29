package com.example.closebysocialize.map


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.closebysocialize.ContainerActivity
import com.example.closebysocialize.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var myPositionImageView: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userHasInteracted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        // Initialize the GoogleMap asynchronously
        mapView.getMapAsync(this)



        myPositionImageView = view.findViewById(R.id.myPositionImageView)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity()) // Initialize fusedLocationClient



    val mapSearchView: SearchView = view.findViewById(R.id.mapSearchView)

        mapSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                performSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()){
                    startPlaceAutocomplete()
                }
                // You can implement suggestions or search as user types, if needed
                return true
            }
        })

        // Set up a focus listener to expand the SearchView when it gains focus
        mapSearchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mapSearchView.isIconified = false
                if (mapSearchView.query.isNullOrEmpty()){
                    startPlaceAutocomplete()
                }
            }
        }

        myPositionImageView.setOnClickListener{
            userHasInteracted = false
            recenterMapOnUserLocation()

        }

        return view
    }

    private fun performSearch(query: String?) {
        if (!query.isNullOrBlank()) {
            // Perform search based on the query
            // For demonstration purposes, we'll just log the search query
            Log.d("PerformSearch", "Search query: $query")
            controlLocationUpdates(true)

            // You can perform other actions here, such as displaying search results
        }
    }

    fun controlLocationUpdates(enable: Boolean) {
        (activity as? ContainerActivity)?.setLocationUpdatesEnabled(enable)
    }

    fun recenterMapOnUserLocation() {
        // Possibly move camera to user's current location
        // Then enable location updates
        controlLocationUpdates(true)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        try {
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.mapstyle
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        addCurrentLocationMarker()

        //longlat for sthlm
        val initialLocation = LatLng(59.3293, 18.0686)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 6f))

        googleMap.uiSettings.isZoomControlsEnabled = true

        googleMap.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                userHasInteracted = true
            }
        }

    }

    private fun addCurrentLocationMarker() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("My Position"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        } else {
            // Since you're handling permissions in the ContainerActivity, you might log or inform the user differently here
            Log.d("MapFragment", "Location permission not granted")
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun updateMapLocation(location: Location) {
        if (!userHasInteracted) {
            val newPos = LatLng(location.latitude, location.longitude)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 12f))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            // Use the selected place (e.g., move camera to the selected location)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLng(place.latLng))
        }

    }
    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1001
    }



    private fun startPlaceAutocomplete() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountry("SE")
            .build(requireContext())

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

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


