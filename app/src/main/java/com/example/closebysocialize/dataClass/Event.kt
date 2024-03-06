package com.example.closebysocialize.dataClass


data class Event(
    var id: String = "",
    val city: String = "",
    val eventType: String = "",
    val title: String = "",
    val location: String = "",
    val day: String = "",
    val time: String = "",
    val date: String = "",
    val description: String = "",
    var attended: Int = 0,
    var spots: Int = 0,
    val authorProfileImageUrl: String = "",
    val authorFirstName: String = "",
    val authorLastName: String = "",
    val authorId: String = "",
    var attendedPeopleProfilePictureUrls: MutableList<String> = mutableListOf(),
    var isSaved: Boolean = false,
    var currentAttendeesCount: Int = 0,
    val createdAt: com.google.firebase.Timestamp? = null,
    val imageUrl: String = "",
    )
