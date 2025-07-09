package com.example.psychologicaltestapp

data class AppointmentRequest(
    val id: String = "",               // <-- este campo
    val userId: String = "",
    val psychologistId: String = "",
    val proposedDate: String = "",
    val proposedTime: String = "",
    val notes: String = "",
    val status: String = "pending",  // pending, accepted, declined, counter_proposed
    val counterProposedDate: String? = null,
    val counterProposedTime: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
