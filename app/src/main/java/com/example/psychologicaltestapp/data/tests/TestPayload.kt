package com.example.psychologicaltestapp.data.tests

data class TestPayload(
    val schema: String? = null,
    val id: String,
    val slug: String,
    val version: String,
    val locale: String,
    val title: String,
    val description: String? = null,
    val instructions: String? = null,
    val ui: Ui? = null,
    val constraints: Constraints? = null,
    val scales: List<Scale> = emptyList(),
    val questions: List<Question> = emptyList(),
    val results: Results
) {
    data class Ui(val theme: String? = null, val progress: String? = null, val shuffleQuestions: Boolean? = null)
    data class Constraints(val timeLimitSec: Int? = null, val allowBackNavigation: Boolean? = null)
    data class Scale(val id: String, val title: String, val method: String? = "sum")

    data class Question(
        val id: String,
        val type: String,
        val prompt: String? = null,
        val required: Boolean? = true,
        val options: List<Option>? = null,
        val scores: List<QuestionScore>? = null,         // (fallback)
        val scoresMatrix: Map<String, List<Int>>? = null, // <-- NUEVO: escala -> [pesos por opción]
        val imageQuestion: String? = null,
        val optionImages: List<String>? = null
    )

    data class Option(
        val id: String,
        val label: String,
        val value: Int? = null // si value no existe, tomaremos índice como valor
    )

    data class QuestionScore(
        val scale: String,
        val mapping: String? = null // "option.value" (por ahora asumimos esta)
    )

    data class Results(
        val scales: List<ResultScale>
    ) {
        data class ResultScale(
            val id: String,
            val ranges: List<Range>
        )
        data class Range(
            val min: Int,
            val max: Int,
            val label: String,
            val color: String? = null,
            val advice: String? = null
        )
    }
}
