package com.example.closebysocialize.chat

data class Comment(
    var id: String = "",
    val displayName: String = "",
    val commentText: String = "",
    val profileImageUrl: String = "",
)