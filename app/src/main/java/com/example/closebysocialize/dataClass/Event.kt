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
    val attended: String = "",
    val spots: String = "",
    val authorProfileImageUrl: String = "",
    val authorFirstName: String = "",
    val authorLastName: String = "",
    val authorId: String = "",
    val attendedPeopleProfilePictureUrls: List<String> = listOf(),
    var isSaved: Boolean = false,
)
