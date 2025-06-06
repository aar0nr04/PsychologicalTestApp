package com.example.psychologicaltestapp


data class Question(
    val questionText: String,
    val options: List<String>,
    val scores: Map<String, List<Int>> // Scores for each category
)