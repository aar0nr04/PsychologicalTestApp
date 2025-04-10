package com.example.psychologicaltestapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Clases de Datos
data class Category(
    val title: String,
    val description: String,
    val tests: List<Test>
)

data class Test(
    val type: String,
    val title: String,
    val questions: List<Question>,
    val results: List<Result>
)

data class Question(
    val questionText: String,
    val options: List<String>,
    val scores: List<Int>
)

data class Result(
    val minScore: Int,
    val maxScore: Int,
    val message: String
)

// Función para Cargar los Tests desde JSON
fun loadTestsFromJson(context: Context): List<Category> {
    val categories = mutableListOf<Category>()

    context.resources.openRawResource(R.raw.tests).use { inputStream ->
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val categoriesArray = jsonObject.getJSONArray("categories")

        for (i in 0 until categoriesArray.length()) {
            val categoryObject = categoriesArray.getJSONObject(i)
            val title = categoryObject.getString("title")
            val description = categoryObject.getString("description")

            val tests = mutableListOf<Test>()
            val testsArray = categoryObject.getJSONArray("tests")
            for (j in 0 until testsArray.length()) {
                val testObject = testsArray.getJSONObject(j)
                val type = testObject.getString("type")
                val testTitle = testObject.getString("title")

                val questions = mutableListOf<Question>()
                val questionsArray = testObject.getJSONArray("questions")
                for (k in 0 until questionsArray.length()) {
                    val questionObject = questionsArray.getJSONObject(k)
                    val questionText = questionObject.getString("questionText")

                    val options = mutableListOf<String>()
                    val optionsArray = questionObject.getJSONArray("options")
                    for (l in 0 until optionsArray.length()) {
                        options.add(optionsArray.getString(l))
                    }

                    val scores = mutableListOf<Int>()
                    val scoresArray = questionObject.getJSONArray("scores")
                    for (l in 0 until scoresArray.length()) {
                        scores.add(scoresArray.getInt(l))
                    }

                    questions.add(Question(questionText, options, scores))
                }

                val results = mutableListOf<Result>()
                val resultsArray = testObject.getJSONArray("results")
                for (k in 0 until resultsArray.length()) {
                    val resultObject = resultsArray.getJSONObject(k)
                    val minScore = resultObject.getInt("minScore")
                    val maxScore = resultObject.getInt("maxScore")
                    val message = resultObject.getString("message")
                    results.add(Result(minScore, maxScore, message))
                }

                tests.add(Test(type, testTitle, questions, results))
            }

            categories.add(Category(title, description, tests))
        }
    }

    return categories
}