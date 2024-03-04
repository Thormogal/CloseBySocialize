package com.example.closebysocialize.dataClass

data class Users(
    var id: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val name: String? = null,
    val savedEvents: List<String> = listOf(),
    val attendingEvents: List<String> = listOf()

)