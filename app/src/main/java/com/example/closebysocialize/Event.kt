package com.example.closebysocialize

data class Event(
    val id: String = "",
    val city: String = "",
    val eventType: String = "",
    val title: String = "",
    val location: String = "",
    val day: String = "",
    val time: String = "",
    val date: String = "",
    val author: String = "",
    val description: String = "",
    val attended: String = "",
    val spots: String = "",
    val profileImageUrl: String = "",
    val authorId: String = "",
    val attendedPeopleProfilePictureUrls: List<String> = listOf()
)
