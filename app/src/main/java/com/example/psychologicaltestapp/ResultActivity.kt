package com.example.psychologicaltestapp

import TestResult
import UserRepository
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Button
import com.example.psychologicaltestapp.databinding.ActivityResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import java.util.*

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var test: Test
    private lateinit var userResponses: List<Int?>
    private var finalResultMessage: String = ""

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

        binding.aiTipsButton.setOnClickListener {
            val prompt = generarPromptConResultados()
            val intent = Intent(this, AiTipsActivity::class.java)
            intent.putExtra("prompt", prompt)
            startActivity(intent)
        }
    }

    private fun showResult() {

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

        val resultMessages = mutableListOf<String>()

        categoryScores.forEach { (category, score) ->
            val matchingResults = test.results?.filter {
                it.category == category && score in it.minScore..it.maxScore
            }

            if (matchingResults != null && matchingResults.isNotEmpty()) {
                val combinedMessage = matchingResults.joinToString("\n\n") { result ->
                    result.message
                }
                resultMessages.add("Categoría: $category\nPuntuación: $score\n$combinedMessage")
            } else {
                resultMessages.add("Categoría: $category\nPuntuación: $score\nResultado no encontrado.")
            }
        }

        val finalResultMessage = resultMessages.joinToString("\n\n")

        // Guardar resultado
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

        // Mostrar resultado
        binding.resultTextView.text = finalResultMessage

        // Botón regresar
        binding.backToMenuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Botón IA Tips
        binding.aiTipsButton.setOnClickListener {

            val prompt = """
            Soy un experto en psicología. Un usuario ha completado el test "${test.title}".
            Estos son sus resultados:

            $finalResultMessage

            Por favor, proporciona 3 recomendaciones prácticas y sencillas, en lenguaje amigable, que puedan ayudarle a mejorar su bienestar emocional.
        """.trimIndent()
        }
    }
    private fun generarPromptConResultados(): String {
        return """
        Soy un experto en psicología. Un usuario ha completado el test "${test.title}".
        Estos son sus resultados:

        $finalResultMessage

        Por favor, proporciona 3 recomendaciones prácticas y sencillas, en lenguaje amigable, que puedan ayudarle a mejorar su bienestar emocional.
    """.trimIndent()
    }


}