package com.example.closebysocialize.dataClass
import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
