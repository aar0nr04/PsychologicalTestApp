package com.example.psychologicaltestapp


data class Question(
    val questionText: String,
    val options: List<String>? = null,
    val optionImages: List<String>? = null,
    val imageQuestion: String? = null,
    val scores: Map<String, List<Int>>? = null // 🔥 Esto es lo que te faltaba
)
