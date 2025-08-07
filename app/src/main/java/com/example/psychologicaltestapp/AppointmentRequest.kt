package com.example.psychologicaltestapp

data class AppointmentRequest(
    val id: String = "",               // <-- este campo
    val userId: String = "",
    val psychologistId: String = "",
    val proposedDate: String = "",
    val proposedTime: String = "",
    val status: String = "",  // pending, accepted, declined, counter_proposed
    val counterProposedDate: String? = null,
    val counterProposedTime: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val date: String = "", // Or the appropriate data type for your date
    val time: String = "",
    val dateTime: String = "", // Changed from 'date' and 'time'// Or the appropriate data type for your time
    val notes: String = "",
)
