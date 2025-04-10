package com.example.psychologicaltestapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class TestActivity : AppCompatActivity() {

    private lateinit var test: Test
    private var currentQuestionIndex = 0
    private var totalScore = 0

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
        val optionsRadioGroup = findViewById<RadioGroup>(R.id.optionsRadioGroup)
        val progressTextView = findViewById<TextView>(R.id.progressTextView)

        // Mostrar la pregunta
        questionTextView.text = question.questionText

        // Limpiar las opciones anteriores
        optionsRadioGroup.removeAllViews()

        // Agregar las opciones como RadioButtons
        question.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(this).apply {
                id = index
                text = option
            }
            optionsRadioGroup.addView(radioButton)
        }

        // Mostrar el progreso
        progressTextView.text = "Pregunta ${currentQuestionIndex + 1} de ${test.questions.size}"
    }

    private fun handleNextButtonClick() {
        // Obtener la respuesta seleccionada
        val optionsRadioGroup = findViewById<RadioGroup>(R.id.optionsRadioGroup)
        val selectedOptionId = optionsRadioGroup.checkedRadioButtonId
        if (selectedOptionId == -1) {
            // Si no se seleccionó ninguna opción, mostrar un mensaje de error
            Toast.makeText(this, "Por favor, selecciona una respuesta", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el índice de la opción seleccionada
        val selectedOptionIndex = optionsRadioGroup.indexOfChild(findViewById(selectedOptionId))

        // Sumar el puntaje correspondiente a la opción seleccionada
        totalScore += test.questions[currentQuestionIndex].scores[selectedOptionIndex]

        // Avanzar a la siguiente pregunta
        currentQuestionIndex++
        showQuestion()
    }

    private fun showResult() {
        // Encontrar el resultado correspondiente al puntaje total
        val result = test.results.find { totalScore in it.minScore..it.maxScore }
        val resultMessage = result?.message ?: "No se pudo calcular el resultado."

        // Mostrar el resultado en un AlertDialog
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Resultado")
        dialogBuilder.setMessage(resultMessage)
        dialogBuilder.setPositiveButton("Aceptar") { _, _ ->
            finish() // Cerrar esta actividad
        }
        dialogBuilder.show()
    }
}