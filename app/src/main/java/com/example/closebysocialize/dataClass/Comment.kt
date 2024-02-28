package com.example.closebysocialize.dataClass

import java.util.Date

data class Comment(
    var id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val commentText: String = "",
    val profileImageUrl: String = "",
    val timestamp: Date = Date(),
    val parentId: String? = null,
    var replies: MutableList<Comment> = mutableListOf(),
    var likes: Int = 0,
    var isLiked: Boolean = false

)