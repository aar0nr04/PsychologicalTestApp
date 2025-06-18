package com.example.psychologicaltestapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Data Classes
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

data class Result(
    val category: String,
    val minScore: Int,
    val maxScore: Int,
    val message: String
)

data class User(
    val userId: String,
    val email: String,
    val name: String,
    val testHistory: List<TestResult> = emptyList() // Store test results here
)

// Function to Load Tests from JSON
fun loadTestsFromJson(context: Context): List<Category> {
    return try {
        val jsonString = context.resources.openRawResource(R.raw.tests).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        parseCategories(jsonObject)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

// Helper Functions for Parsing
private fun parseCategories(jsonObject: JSONObject): List<Category> {
    val categories = mutableListOf<Category>()
    val categoriesArray = jsonObject.getJSONArray("categories")

    for (i in 0 until categoriesArray.length()) {
        val categoryObject = categoriesArray.getJSONObject(i)
        categories.add(parseCategory(categoryObject))
    }

    return categories
}

private fun parseCategory(categoryObject: JSONObject): Category {
    val title = categoryObject.optString("title", "Título no disponible")
    val description = categoryObject.optString("description", "Descripción no disponible")
    val tests = categoryObject.optJSONArray("tests")?.let { parseTests(it) } ?: emptyList()

    return Category(title, description, tests)
}

private fun parseTests(testsArray: JSONArray): List<Test> {
    val tests = mutableListOf<Test>()

    for (j in 0 until testsArray.length()) {
        val testObject = testsArray.getJSONObject(j)
        tests.add(parseTest(testObject))
    }

    return tests
}

private fun parseTest(testObject: JSONObject): Test {
    val type = testObject.optString("type", "Tipo no disponible")
    val title = testObject.optString("title", "Título del test no disponible")
    val questions = testObject.optJSONArray("questions")?.let { parseQuestions(it) } ?: emptyList()
    val results = testObject.optJSONArray("results")?.let { parseResults(it) } ?: emptyList()

    return Test(type, title, questions, results)
}

private fun parseQuestions(questionsArray: JSONArray): List<Question> {
    val questions = mutableListOf<Question>()

    for (k in 0 until questionsArray.length()) {
        val questionObject = questionsArray.getJSONObject(k)
        questions.add(parseQuestion(questionObject))
    }

    return questions
}

private fun parseQuestion(questionObject: JSONObject): Question {
    val questionText = questionObject.optString("questionText", "Pregunta no disponible")
    val options = parseOptions(questionObject.optJSONArray("options"))
    val optionImages = parseOptions(questionObject.optJSONArray("optionImages")) // Reutilizas misma función
    val imageQuestion = questionObject.optString("imageQuestion", null)
    val scores = parseScores(questionObject.optJSONObject("scores"))

    return Question(
        questionText = questionText,
        options = options,
        optionImages = optionImages,
        imageQuestion = imageQuestion,
        scores = scores
    )
}

private fun parseOptions(optionsArray: JSONArray?): List<String> {
    val options = mutableListOf<String>()

    if (optionsArray != null) {
        for (l in 0 until optionsArray.length()) {
            options.add(optionsArray.optString(l, "Opción no disponible"))
        }
    }

    return options
}

private fun parseScores(scoresObject: JSONObject?): Map<String, List<Int>> {
    val scores = mutableMapOf<String, List<Int>>()

    if (scoresObject != null) {
        for (key in scoresObject.keys()) {
            val scoreValues = mutableListOf<Int>()
            val scoreArray = scoresObject.optJSONArray(key)

            if (scoreArray != null) {
                for (l in 0 until scoreArray.length()) {
                    scoreValues.add(scoreArray.optInt(l, 0))
                }
            }

            scores[key] = scoreValues
        }
    }

    return scores
}

private fun parseResults(resultsArray: JSONArray): List<Result> {
    val results = mutableListOf<Result>()

    for (k in 0 until resultsArray.length()) {
        val resultObject = resultsArray.getJSONObject(k)
        val category = resultObject.optString("category", "Categoría no disponible")
        val minScore = resultObject.optInt("minScore", 0)
        val maxScore = resultObject.optInt("maxScore", 0)
        val message = resultObject.optString("message", "Mensaje no disponible")

        results.add(Result(category, minScore, maxScore, message))
    }

    return results
}