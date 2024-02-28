package com.example.closebysocialize.map


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.annotation.RequiresApi
import com.example.closebysocialize.ContainerActivity
import com.example.closebysocialize.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.common.io.Resources

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
            }
        }

        return view
    }

    private fun performSearch(query: String?) {
        // Check if the query is not null or empty
        if (!query.isNullOrBlank()) {
            // Perform search based on the query
            // For demonstration purposes, we'll just log the search query
            Log.d("PerformSearch", "Search query: $query")

            // You can perform other actions here, such as displaying search results
        }
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

        //longlat for sthlm
        val initialLocation = LatLng(59.3293, 18.0686)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 6f))
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun updateMapLocation(location: Location) {
        val newPos = LatLng(location.latitude, location.longitude)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 15f))
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


