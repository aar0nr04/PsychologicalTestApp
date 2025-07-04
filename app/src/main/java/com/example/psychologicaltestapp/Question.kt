package com.example.psychologicaltestapp


data class Question(
    val questionText: String?,
    val options: List<String>?,
    val optionImages: List<String>?,
    val imageQuestion: String?,
    val scores: Map<String, List<Int>>?,  // Para tests con categorías
    val scoresArray: List<Int>?           // Para tests sin categorías
)
