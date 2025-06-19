package com.example.psychologicaltestapp

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TestActivity : AppCompatActivity() {

    private lateinit var test: Test
    private var currentQuestionIndex = 0
    private val userResponses = mutableListOf<Int?>() // Lista para almacenar respuestas (nullable)
    private var selectedOptionIndex: Int? = null // Índice de la opción seleccionada (nullable)
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: Button
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // Vincular vistas
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)
        nextButton = findViewById(R.id.nextButton)
        nextButton.isEnabled = false // Deshabilitar "Siguiente" inicialmente

        try {
            // Obtener el tipo de test desde el Intent
            val testType = intent.getStringExtra("TEST_TYPE")
                ?: throw IllegalArgumentException("Test type not provided")

            // Cargar todos los tests desde el archivo JSON
            val categories = loadTestsFromJson(this)
            val tests = categories.flatMap { it.tests }

            // Encontrar el test correspondiente al tipo
            test = tests.find { it.type == testType }
                ?: throw IllegalArgumentException("Test not found: $testType")

            // Inicializar la lista de respuestas con null para cada pregunta
            userResponses.addAll(List(test.questions.size) { null })

            // Mostrar la primera pregunta
            showQuestion()

            // Configurar botones
            backButton.setOnClickListener {
                handleBackButtonClick()
            }
            nextButton.setOnClickListener {
                handleNextButtonClick()
            }

        } catch (e: Exception) {
            // Mostrar un mensaje de error si algo falla
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish() // Cerrar la actividad
        }
    }

    private fun showQuestion() {
        if (currentQuestionIndex >= test.questions.size) {
            showResult()
            return
        }

        val question = test.questions[currentQuestionIndex]
        val questionTextView = findViewById<TextView>(R.id.questionTextView)
        val optionsContainer = findViewById<LinearLayout>(R.id.optionsContainer)
        val progressTextView = findViewById<TextView>(R.id.progressTextView)

        questionTextView.text = question.questionText
        optionsContainer.removeAllViews()

        val optionImages = question.optionImages
        if (!optionImages.isNullOrEmpty()) {
            // 🔍 Mostrar imágenes como opciones
            optionImages.forEachIndexed { index, imagePath ->
                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        600 // Ajusta a tu diseño
                    ).apply {
                        setMargins(0, 16, 0, 16)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setOnClickListener {
                        handleOptionSelected(index)
                    }
                    contentDescription = "Opción imagen $index"
                }

                try {
                    Log.d("TEST_ASSETS", "Intentando abrir assets: $imagePath")
                    val inputStream = assets.open(imagePath)
                    Log.d("TEST_ASSETS", "✅ Abrió con éxito: $imagePath")
                    val drawable = Drawable.createFromStream(inputStream, null)
                    imageView.setImageDrawable(drawable)
                } catch (e: Exception) {
                    Log.e("TEST_ASSETS", "❌ No se pudo abrir: $imagePath", e)
                }

                optionsContainer.addView(imageView)
            }

        } else {
            // ✍️ Mostrar texto como opciones
            question.options?.forEachIndexed { index, option ->
                val button = Button(this).apply {
                    text = option
                    textSize = 16f
                    setPadding(32, 16, 32, 16)
                    setBackgroundResource(R.drawable.custom_button_style)
                    tag = index
                    setOnClickListener {
                        handleOptionSelected(index)
                    }
                    contentDescription = "Opción $index"
                }

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                button.layoutParams = layoutParams

                optionsContainer.addView(button)
            }
        }

        val previousResponse = userResponses[currentQuestionIndex]
        if (previousResponse != null) {
            handleOptionSelected(previousResponse)
        } else {
            selectedOptionIndex = null
            nextButton.isEnabled = false
        }

        val progressPercent = ((currentQuestionIndex + 1) * 100) / test.questions.size
        progressTextView.text = "Pregunta ${currentQuestionIndex + 1} de ${test.questions.size}"
        progressBar.progress = progressPercent
    }


    private fun handleNextButtonClick() {
        val selectedOptionIndex = this.selectedOptionIndex
        if (selectedOptionIndex == null) {
            // Mostrar un mensaje de error si no se seleccionó ninguna opción
            Toast.makeText(this, "Por favor, selecciona una respuesta", Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar la respuesta del usuario
        userResponses[currentQuestionIndex] = selectedOptionIndex

        // Avanzar a la siguiente pregunta
        currentQuestionIndex++
        showQuestion()
    }

    private fun handleBackButtonClick() {
        if (currentQuestionIndex > 0) {
            // Retroceder a la pregunta anterior
            currentQuestionIndex--
            showQuestion()
        } else {
            // Si ya estamos en la primera pregunta, mostrar un mensaje
            Toast.makeText(this, "Ya estás en la primera pregunta.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleOptionSelected(index: Int) {
        // Actualizar el índice de la opción seleccionada
        selectedOptionIndex = index

        // Habilitar el botón "Siguiente"
        nextButton.isEnabled = true

        // Obtener el contenedor que contiene todos los botones
        val optionsContainer = findViewById<LinearLayout>(R.id.optionsContainer)

        // Recorrer todos los botones para actualizar su apariencia
        for (i in 0 until optionsContainer.childCount) {
            val button = optionsContainer.getChildAt(i) as Button
            button.setBackgroundResource(if (i == index) R.drawable.custom_button_selected else R.drawable.custom_button_style)
        }
    }

    private fun showResult() {
        // Step 1: Calcular puntajes para cada categoría
        val categoryScores = mutableMapOf<String, Int>() // Mapa para almacenar puntajes por categoría

        test.questions.forEachIndexed { questionIndex, question ->
            question.scores?.forEach { (category, scoreValues) ->
                // Obtener el índice de la opción seleccionada para la pregunta actual
                val selectedOptionIndex = userResponses[questionIndex] ?: -1 // Default to -1 for unanswered questions
                if (selectedOptionIndex == -1) {
                    println("Warning: No response recorded for question $questionIndex")
                    return@forEachIndexed
                }
                val scoreToAdd = scoreValues[selectedOptionIndex]
                categoryScores[category] = (categoryScores[category] ?: 0) + scoreToAdd
            }
        }

        // Step 2: Generar mensajes de resultado para cada categoría
        val resultMessages = mutableListOf<String>()

        categoryScores.forEach { (category, score) ->
            // Encontrar todos los resultados que coincidan con la categoría y el puntaje
            val matchingResults = test.results.filter { it.category == category && score in it.minScore..it.maxScore }

            if (matchingResults.isNotEmpty()) {
                // Combinar todos los mensajes de resultado en una sola cadena
                val combinedMessage = matchingResults.joinToString("\n\n") { it.message }
                resultMessages.add("Categoría: $category\nPuntuación: $score\n$combinedMessage")
            } else {
                resultMessages.add("Categoría: $category\nPuntuación: $score\nResultado no encontrado.")
            }
        }

        // Step 3: Combinar todos los mensajes de resultado en un solo texto
        val finalResultMessage = if (resultMessages.isNotEmpty()) {
            resultMessages.joinToString("\n\n")
        } else {
            "No se pudo calcular el resultado."
        }

        // Step 4: Pasar el mensaje de resultado a ResultActivity
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Guardar el resultado en Firestore si el usuario está autenticado
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val userRepository = UserRepository()
                    val testResult = TestResult(
                        testType = test.type,
                        testName = test.title,
                        resultMessage = finalResultMessage,
                        date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    )
                    userRepository.saveTestResult(currentUser.uid, testResult)
                }
            } catch (e: Exception) {
                println("Error saving test result: ${e.message}")
            }
        }

        // Paso 5: Navegar a ResultActivity con datos necesarios
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("TEST_JSON", Gson().toJson(test)) // Pasar el objeto Test como JSON
            putExtra("USER_RESPONSES", Gson().toJson(userResponses)) // Pasar las respuestas del usuario como JSON
        }
        startActivity(intent)
        finish() // Finalizar TestActivity para que el usuario no pueda volver a las preguntas
    }
}