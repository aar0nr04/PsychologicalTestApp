package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class TestActivity : AppCompatActivity() {

    private lateinit var test: Test
    private var currentQuestionIndex = 0
    private var totalScore = 0
    // Add this variable to track the selected option
    private var selectedOptionIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

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

            // Mostrar la primera pregunta
            showQuestion()

            // Configurar el botón "Regresar"
            val backButton = findViewById<Button>(R.id.backButton)
            backButton.setOnClickListener {
                finish() // Cerrar esta actividad y regresar al menú anterior
            }

            // Configurar el botón "Siguiente"
            val nextButton = findViewById<Button>(R.id.nextButton)
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
            // Mostrar resultados si no hay más preguntas
            showResult()
            return
        }

        val question = test.questions[currentQuestionIndex]

        // Vincular las vistas
        val questionTextView = findViewById<TextView>(R.id.questionTextView)
        val optionsContainer = findViewById<LinearLayout>(R.id.optionsContainer)
        val progressTextView = findViewById<TextView>(R.id.progressTextView)

        // Mostrar la pregunta
        questionTextView.text = question.questionText

        // Limpiar las opciones anteriores
        optionsContainer.removeAllViews()

        // Agregar las opciones como botones estilizados
        question.options.forEachIndexed { index, option ->
            val button = Button(this).apply {
                text = option
                textSize = 16f // Tamaño de texto más grande
                setPadding(32, 16, 32, 16) // Espaciado interno para hacer el botón más grande
                setBackgroundResource(R.drawable.custom_button_style) // Estilo personalizado
                tag = index // Usar el índice como tag para identificar la opción seleccionada
                setOnClickListener {
                    handleOptionSelected(index) // Manejar la selección de la opción
                }
            }

            // Configurar los parámetros de diseño para agregar espacio entre los botones
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8) // Margen superior e inferior entre botones
            }
            button.layoutParams = layoutParams

            optionsContainer.addView(button)
        }

        // Mostrar el progreso
        progressTextView.text = "Pregunta ${currentQuestionIndex + 1} de ${test.questions.size}"
    }
    private fun handleNextButtonClick() {
        val selectedOptionIndex = this.selectedOptionIndex
        if (selectedOptionIndex == null) {
            // Si no se seleccionó ninguna opción, mostrar un mensaje de error
            Toast.makeText(this, "Por favor, selecciona una respuesta", Toast.LENGTH_SHORT).show()
            return
        }

        // Sumar el puntaje correspondiente a la opción seleccionada
        totalScore += test.questions[currentQuestionIndex].scores[selectedOptionIndex]

        // Avanzar a la siguiente pregunta
        currentQuestionIndex++
        showQuestion()
    }


    private fun handleOptionSelected(index: Int) {
        // Update the selected option index
        selectedOptionIndex = index

        // Get the container holding all the buttons
        val optionsContainer = findViewById<LinearLayout>(R.id.optionsContainer)

        // Loop through all buttons to update their appearance
        for (i in 0 until optionsContainer.childCount) {
            val button = optionsContainer.getChildAt(i) as Button
            button.setBackgroundResource(if (i == index) R.drawable.custom_button_selected else R.drawable.custom_button_style)
        }
    }
    private fun showResult() {
        // Encontrar el resultado correspondiente al puntaje total
        val result = test.results.find { totalScore in it.minScore..it.maxScore }
        val resultMessage = result?.message ?: "No se pudo calcular el resultado."

        // En lugar de mostrar un AlertDialog, iniciamos ResultActivity
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("RESULT_MESSAGE", resultMessage)
        }
        startActivity(intent)
        finish() // Finaliza TestActivity para que el usuario no pueda volver a las preguntas
    }
}