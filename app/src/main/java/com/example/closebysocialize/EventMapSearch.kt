package com.example.closebysocialize;

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.closebysocialize.message.FireBaseMessagingService.Companion.TAG
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener


class EventMapSearch : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_map_search)

        // Initialize the SDK
        Places.initialize(applicationContext, "AIzaSyCj1cLlJFyBL7gfgpURZzCSWw9B1evS-zE")

        // Create a new Places client instance
        val placesClient = Places.createClient(this)

        // Initialize the AutocompleteSupportFragment
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )

        // Set up a PlaceSelectionListener to handle the response
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Get the place's name and coordinates
                val placeName = place.name
                val placeCoordinates = place.latLng

                val resultIntent = Intent().apply {
                    putExtra("place_name", placeName)
                    putExtra("place_coordinates", placeCoordinates)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }
}