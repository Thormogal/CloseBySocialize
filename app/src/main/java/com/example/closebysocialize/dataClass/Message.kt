package com.example.closebysocialize.dataClass
import java.util.Date

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val type: String = "text",
    val isRead: Boolean = false,
    val messageStatus: String = "sent"
)
