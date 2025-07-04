package com.example.psychologicaltestapp

data class Appointment(
    val userId: String = "",
    val psychologistId: String = "",
    val date: String = "",
    val time: String = "",
    val notes: String = ""
)
