package com.example.psychologicaltestapp

data class TestResult(
    val testName: String = "",
    val date: String = "",
    val score: String = "",
    val testJson: String? = null,
    val userResponsesJson: String? = null,
    val testType: String = "",
    val resultMessage: String = "",
    val userId: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)
