package com.example.closebysocialize.map


import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.closebysocialize.ContainerActivity
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Event
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.firestore.GeoPoint


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var myPositionImageView: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userHasInteracted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        myPositionImageView = view.findViewById(R.id.myPositionImageView)
        myPositionImageView.setOnClickListener {
            userHasInteracted = false
            recenterMapOnUserLocation()
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupSearchView(view)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.title_map)
    }

    private fun setupSearchView(view: View) {
        val mapSearchView: SearchView = view.findViewById(R.id.mapSearchView)
        mapSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
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
        controlLocationUpdates(true)
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

        googleMap.setOnMarkerClickListener { marker ->
            val eventId = marker.tag as? String
            eventId?.let {
                fetchEventDetails(it)
            }
            true
        }

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
        db.collection("events")
            .get()
            .addOnSuccessListener { eventDocuments ->
                Log.d("Firestore", "Fetched ${eventDocuments.size()} event documents")
                for (eventDocument in eventDocuments) {
                    val placeCoordinates = eventDocument.getGeoPoint("place_coordinates")
                    if (placeCoordinates != null) {
                        addMarkerForPlace(eventDocument.id, placeCoordinates)
                    } else {
                        Log.d(
                            "Firestore",
                            "No place_coordinates found for event ${eventDocument.id}"
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Error getting event documents: ", exception)
            }
    }

    fun fetchEventDetails(eventId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events").document(eventId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val event = document.toObject(Event::class.java) // Antag att Event är din dataklass
                event?.let {
                    showEventDialog(it)
                }
            } else {
                Log.d("Firestore", "No event found with ID: $eventId")
            }
        }.addOnFailureListener { exception ->
            Log.d("Firestore", "Error getting event details: ", exception)
        }
    }

    fun addMarkerForPlace(eventId: String, geoPoint: GeoPoint) {
        val markerOptions = MarkerOptions()
            .position(LatLng(geoPoint.latitude, geoPoint.longitude))
            .title("Event ID: $eventId")
        val marker = googleMap.addMarker(markerOptions)
        if (marker != null) {
            marker.tag = eventId
        }

        googleMap.addMarker(markerOptions)
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
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("My location"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
                }
            }
        } else {
            Log.d("MapFragment", "Location permission not granted")
        }
    }

    fun updateMapLocation(location: Location) {
        if (!userHasInteracted && ::googleMap.isInitialized) {
            val newPos = LatLng(location.latitude, location.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 10f))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            userHasInteracted = true
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(place.latLng))
        }
    }

    fun showEventDialog(event: Event) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.item_event)
        val eventNameTextView = dialog.findViewById<TextView>(R.id.eventNameTextView)
        eventNameTextView.text = event.id

        dialog.show()
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
        controlLocationUpdates(true)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }



    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        controlLocationUpdates(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()

    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
