package com.example.closebysocialize.dataClass
import java.util.Date

data class Message(
    val senderId: String = "",
    val content: String = "",
    val timestamp: Date = Date()
)
