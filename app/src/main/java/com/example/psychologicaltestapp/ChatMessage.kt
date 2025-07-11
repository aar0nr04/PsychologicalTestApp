package com.example.psychologicaltestapp

import com.google.firebase.Timestamp

data class ChatMessage(
    val fromUserId: String = "",
    val toPsychologistId: String = "",
    val appointmentId: String = "",
    val messageText: String = "",
    val timestamp: Timestamp? = null
)
