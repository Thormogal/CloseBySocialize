package com.example.closebysocialize.dataClass

data class Friend(
    var id: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val name: String? = null,
    val isRequest: Boolean = false,
    val requestId: String = "",
    val user: Users = Users()

)