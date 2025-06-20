package com.example.psychologicaltestapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import com.example.psychologicaltestapp.databinding.ActivityResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import java.util.*

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var test: Test
    private lateinit var userResponses: List<Int?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout usando View Binding
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener los datos del Intent
        val testJson = intent.getStringExtra("TEST_JSON")
            ?: throw IllegalArgumentException("Test data not provided")
        val responsesJson = intent.getStringExtra("USER_RESPONSES")
            ?: throw IllegalArgumentException("User responses not provided")

        // Convertir JSON a objetos
        test = Gson().fromJson(testJson, Test::class.java)
        userResponses = Gson().fromJson(responsesJson, Array<Int?>::class.java).toList()

        // Calcular y mostrar resultados
        showResult()
    }

    private fun showResult() {


        // Step 1: Calculate scores for each category
        val categoryScores = mutableMapOf<String, Int>()

        test.questions.forEachIndexed { questionIndex, question ->
            val scoresMap = question.scores ?: return@forEachIndexed
            scoresMap.forEach { (category, scoreValues) ->
                val selectedOptionIndex = userResponses[questionIndex]
                    ?: throw IllegalStateException("No response recorded for question $questionIndex")
                val scoreToAdd = scoreValues[selectedOptionIndex]
                categoryScores[category] = (categoryScores[category] ?: 0) + scoreToAdd
            }
        }
// Step 2: Generate result messages
        val resultMessages = mutableListOf<String>()
        categoryScores.forEach { (category, score) ->
            val matchingResults = test.results?.filter {
                // Assuming Result class has non-nullable category, minScore, maxScore
                it.category == category && score in it.minScore..it.maxScore
            }

            // Check if matchingResults is NOT null AND THEN if it's not empty
            if (matchingResults != null && matchingResults.isNotEmpty()) {
                // Inside this block, Kotlin smart-casts matchingResults to a non-nullable List<Result>
                val combinedMessage = matchingResults.joinToString("\n\n") { result ->
                    result.message // Assuming Result.message is non-nullable
                }
                resultMessages.add("Categoría: $category\nPuntuación: $score\n$combinedMessage")
            } else {
                // This 'else' covers two cases:
                // 1. test.results was null (so matchingResults became null)
                // 2. test.results was not null, but the filter found no matches (matchingResults was an empty list)
                resultMessages.add("Categoría: $category\nPuntuación: $score\nResultado no encontrado.")
            }
            // Step 3: Combine all result messages into a single text
            val finalResultMessage = resultMessages.joinToString("\n\n")

            // Step 4: Save the result to the user's profile
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val testResult = TestResult(
                    testType = test.type,
                    testName = test.title,
                    resultMessage = finalResultMessage,
                    date = DateFormat.format("yyyy-MM-dd HH:mm:ss", Date()).toString()
                )
                val userRepository = UserRepository()
                userRepository.saveTestResult(currentUser.uid, testResult)
            }

            // Step 5: Show the result to the user
            binding.resultTextView.text = finalResultMessage

            // Manejar el botón "Regresar al Menú Principal"
            binding.backToMenuButton.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Finaliza esta actividad para evitar que el usuario regrese aquí al presionar "Atrás"
            }
        }
    }
}