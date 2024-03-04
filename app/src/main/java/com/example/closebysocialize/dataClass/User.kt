package com.example.closebysocialize.dataClass

data class User(
    var id: String = "",
    var name: String? = null,
    var email: String? = null,
    var profileImageUrl: String? = null,
    var savedEvents: List<String> = listOf(),
    var attendingEvents: List<String> = listOf()
)
