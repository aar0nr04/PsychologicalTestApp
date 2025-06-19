package com.example.psychologicaltestapp


data class Question(
    val questionText: String? = null,          // texto de la pregunta (corregido)
    val text: String? = null,             // texto de la pregunta
    val imageQuestion: String? = null,    // imagen principal
    val options: List<String>? = null,    // textos de las opciones
    val optionImages: List<String>? = null, // imágenes de las opciones
    val scores: Map<String, List<Int>>? = null  // Asegúrate de tener esto
)
