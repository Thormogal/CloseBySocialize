package com.example.closebysocialize.map


import android.Manifest
import android.annotation.SuppressLint
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.example.closebysocialize.EventPlace


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
        mapView.getMapAsync(this)

        myPositionImageView = view.findViewById(R.id.myPositionImageView)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())


        val mapSearchView: SearchView = view.findViewById(R.id.mapSearchView)

        mapSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                performSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    startPlaceAutocomplete()
                }
                return true
            }
        })

        mapSearchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mapSearchView.isIconified = false
                if (mapSearchView.query.isNullOrEmpty()) {
                    startPlaceAutocomplete()
                }
            }
        }

        myPositionImageView.setOnClickListener {
            userHasInteracted = false
            recenterMapOnUserLocation()

        }

        return view
    }

    private fun performSearch(query: String?) {
        if (!query.isNullOrBlank()) {
            // For demonstration purposes, we'll just log the search query
            Log.d("PerformSearch", "Search query: $query")
            controlLocationUpdates(true)
        }
    }

    fun controlLocationUpdates(enable: Boolean) {
        (activity as? ContainerActivity)?.setLocationUpdatesEnabled(enable)
    }

    fun recenterMapOnUserLocation() {
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

        // longlat for sthlm
        val initialLocation = LatLng(59.3293, 18.0686)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 6f))
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                userHasInteracted = true
            }
        }
        fetchPlacesAndDisplayOnMap()
    }

    fun fetchPlacesAndDisplayOnMap() {
        val db = FirebaseFirestore.getInstance()
        db.collection("place_coordinates")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("Firestore", "Fetched ${documents.size()} documents")
                for (document in documents) {
                    val eventPlace = document.toObject(EventPlace::class.java)
                    addMarkerForPlace(eventPlace)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Error getting documents: ", exception)
            }
    }

    fun addMarkerForPlace(eventPlace: EventPlace) {
        // Convert Firestore GeoPoint to LatLng
        val latLng = LatLng(eventPlace.place_coordinates.latitude, eventPlace.place_coordinates.longitude)
        googleMap.addMarker(MarkerOptions().position(latLng).title("Custom Place"))
    }
    private fun addCurrentLocationMarker() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title(""))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
                }
            }
        } else {
            Log.d("MapFragment", "Location permission not granted")
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun updateMapLocation(location: Location) {
        if (!userHasInteracted) {
            val newPos = LatLng(location.latitude, location.longitude)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 10f))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            userHasInteracted= true
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


