package com.example.psychologicaltestapp

import TestResult
import UserRepository
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Toast
import com.example.psychologicaltestapp.databinding.ActivityResultBinding
import com.example.psychologicaltestapp.utils.DialogHelper
import com.example.psychologicaltestapp.utils.PremiumManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.util.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var test: Test
    private var userResponses: List<Int?> = emptyList()
    private var finalResultMessage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout usando View Binding
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar AdMob
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        // Obtener datos del Intent con validación
        val testJson = intent.getStringExtra("TEST_JSON")
        val responsesJson = intent.getStringExtra("USER_RESPONSES")

        if (testJson.isNullOrBlank() || responsesJson.isNullOrBlank()) {
            Toast.makeText(this, "Datos del test incompletos o inválidos.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        try {
            test = Gson().fromJson(testJson, Test::class.java)
            userResponses = Gson().fromJson(responsesJson, Array<Int?>::class.java).toList()
        } catch (e: JsonSyntaxException) {
            Toast.makeText(this, "Error al interpretar los datos del test.", Toast.LENGTH_LONG).show()
            finish()
            return
        } catch (e: Exception) {
            Toast.makeText(this, "Error inesperado: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Calcular y mostrar resultados
        showResult()

        // Botón: Regresar al Menú
        binding.backToMenuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Botón: IA Tips
        binding.aiTipsButton.setOnClickListener {
            if (PremiumManager.isUserPremium(this)) {
                val prompt = generarPromptConResultados()
                val intent = Intent(this, AiTipsActivity::class.java)
                intent.putExtra("prompt", prompt)
                startActivity(intent)
            } else {
                DialogHelper.mostrarDialogoPremium(this)
            }
        }

        // Botón: Consulta un Experto
        binding.psychologistButton.setOnClickListener {
            val intent = Intent(this, PsychologistDirectoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showResult() {
        val categoryScores = mutableMapOf<String, Int>()

        try {
            test.questions.forEachIndexed { questionIndex, question ->
                val scoresMap = question.scores ?: return@forEachIndexed
                val selectedOptionIndex = userResponses.getOrNull(questionIndex)
                    ?: throw IllegalStateException("No hay respuesta para la pregunta $questionIndex")

                scoresMap.forEach { (category, scoreValues) ->
                    val scoreToAdd = scoreValues.getOrNull(selectedOptionIndex) ?: 0
                    categoryScores[category] = (categoryScores[category] ?: 0) + scoreToAdd
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error calculando resultados: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return
        }

        val resultMessages = mutableListOf<String>()
        categoryScores.forEach { (category, score) ->
            val matchingResults = test.results.filter {
                it.category == category && score in it.minScore..it.maxScore
            }

            if (matchingResults.isNotEmpty()) {
                val combinedMessage = matchingResults.joinToString("\n\n") { it.message }
                resultMessages.add("Categoría: $category\nPuntuación: $score\n$combinedMessage")
            } else {
                resultMessages.add("Categoría: $category\nPuntuación: $score\nResultado no encontrado.")
            }
        }

        finalResultMessage = resultMessages.joinToString("\n\n")
        binding.resultTextView.text = finalResultMessage

        // Guardar resultado en Firestore
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
