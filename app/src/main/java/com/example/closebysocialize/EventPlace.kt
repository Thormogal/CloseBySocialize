package com.example.closebysocialize

import com.google.firebase.firestore.GeoPoint

data class EventPlace(

    val place_coordinates: GeoPoint = GeoPoint(0.0,0.0)

)
