package com.example.psychologicaltestapp

import TestResult
import UserRepository
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError

class TestActivity : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var test: Test
    private var currentQuestionIndex = 0
    private val userResponses = mutableListOf<Int?>() // Lista para almacenar respuestas (nullable)
    private var selectedOptionIndex: Int? = null // Índice de la opción seleccionada (nullable)
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var progressTextView: TextView
    private lateinit var questionImage: ImageView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var optionsGrid: GridLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        loadInterstitialAd()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)



        Log.d("TestActivity", "Interstitial ad loading initiated")

        // Vincular vistas
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)
        nextButton = findViewById(R.id.nextButton)
        nextButton.isEnabled = false // Deshabilitar "Siguiente" inicialmente
        progressTextView = findViewById(R.id.progressTextView)
        questionImage = findViewById(R.id.questionImage)
        optionsContainer = findViewById(R.id.optionsContainer)
        optionsGrid = findViewById(R.id.optionsGrid)
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
    private fun loadInterstitialAd() {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", request, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                mInterstitialAd = ad
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        startResultActivity() // cuando el ad se cierra, muestra resultados
                        mInterstitialAd = null
                    }
                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        startResultActivity()
                        mInterstitialAd = null
                    }
                }
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                mInterstitialAd = null
            }
        })
    }
    private fun showInterstitialOrResults() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            startResultActivity()
        }
    }
    private fun showQuestion() {
        if (currentQuestionIndex >= test.questions.size) {
            showResult()
            return
        }

        val question = test.questions.get(currentQuestionIndex)
        val questionTextView = findViewById<TextView>(R.id.questionTextView)
        val questionImage = findViewById<ImageView>(R.id.questionImage)
        val optionsGrid = findViewById<GridLayout>(R.id.optionsGrid)

        // Limpiar vistas anteriores
        optionsGrid.removeAllViews()

// Mostrar texto de la pregunta (si hay)
        if (!question.questionText.isNullOrEmpty()) {
            questionTextView.text = question.questionText
            questionTextView.visibility = View.VISIBLE
        } else {
            questionTextView.visibility = View.GONE
        }
        // Mostrar imagen de la pregunta (si hay)
        question.imageQuestion?.let {
            val bmp = assets.open(it).use { BitmapFactory.decodeStream(it) }
            questionImage.setImageBitmap(bmp)
            questionImage.visibility = View.VISIBLE
        } ?: run {
            questionImage.visibility = View.GONE
        }

        // Mostrar opciones si hay imágenes o texto
        if (!question.optionImages.isNullOrEmpty() || !question.options.isNullOrEmpty()) {
            optionsGrid.visibility = View.VISIBLE

            val totalOptions = maxOf(
                question.options?.size ?: 0,
                question.optionImages?.size ?: 0
            )

            val optionLayouts = mutableListOf<LinearLayout>()

            for (i in 0 until totalOptions) {
                val optionLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(8, 8, 8, 8)
                    layoutParams = ViewGroup.MarginLayoutParams(300, 300)
                    setBackgroundResource(R.drawable.normal_option_background)
                }

                question.optionImages?.getOrNull(i)?.let { imagePath ->
                    val imgView = ImageView(this).apply {
                        setImageBitmap(
                            assets.open(imagePath).use { BitmapFactory.decodeStream(it) })
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 200
                        )
                    }
                    optionLayout.addView(imgView)
                }

                question.options?.getOrNull(i)?.let { text ->
                    val textView = TextView(this).apply {
                        this.text = text
                        gravity = Gravity.CENTER
                    }
                    optionLayout.addView(textView)
                }

                optionLayout.setOnClickListener {
                    handleOptionSelected(i)
                    updateSelectedOptionUI(i, optionLayouts)
                }

                optionsGrid.addView(optionLayout)
                optionLayouts.add(optionLayout)
            }

            // Restaurar selección previa si existe
            val previousResponse = userResponses[currentQuestionIndex]
            if (previousResponse != null) {
                selectedOptionIndex = previousResponse
                updateSelectedOptionUI(previousResponse, optionLayouts)
                nextButton.isEnabled = true
            } else {
                selectedOptionIndex = null
                nextButton.isEnabled = false
            }
        }

        var progressPercent = ((currentQuestionIndex + 1) * 100) / test.questions.size
        progressTextView.text = "Pregunta ${currentQuestionIndex + 1} de ${test.questions.size}"
        progressBar.progress = progressPercent




        val previousResponse = userResponses[currentQuestionIndex]
        if (previousResponse != null) {
            handleOptionSelected(previousResponse)
        } else {
            selectedOptionIndex = null
            nextButton.isEnabled = false
        }

        progressPercent = ((currentQuestionIndex + 1) * 100) / test.questions.size
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

        if (test.results.any { it.category.isNullOrEmpty() }) {
            // Test sin categorías, como TONI

            var totalScore = 0

            test.questions.forEachIndexed { index, question ->
                val selectedOptionIndex = userResponses[index] ?: -1
                if (selectedOptionIndex == -1) return@forEachIndexed
                val scoresArray = question.scoresArray ?: return@forEachIndexed
                if (selectedOptionIndex < scoresArray.size) {
                    totalScore += scoresArray[selectedOptionIndex]
                }
            }

            val matchingResult = test.results.find { totalScore in it.minScore..it.maxScore }

            val finalResultMessage = if (matchingResult != null) {
                "Puntuación total: $totalScore\n${matchingResult.message}"
            } else {
                "Puntuación total: $totalScore\nResultado no encontrado."
            }

            guardarYMostrarResultado(finalResultMessage)

        } else {
            // Test por categorías (actual funcionamiento)
            val categoryScores = mutableMapOf<String, Int>()

            test.questions.forEachIndexed { questionIndex, question ->
                question.scores?.forEach { (category, scoreValues) ->
                    val selectedOptionIndex = userResponses[questionIndex] ?: -1
                    if (selectedOptionIndex == -1) return@forEachIndexed
                    val scoreToAdd = scoreValues[selectedOptionIndex]
                    categoryScores[category] = (categoryScores[category] ?: 0) + scoreToAdd
                }
            }

            val resultMessages = categoryScores.map { (category, score) ->
                val matchingResults = test.results.filter { it.category == category && score in it.minScore..it.maxScore }
                if (matchingResults.isNotEmpty()) {
                    val combinedMessage = matchingResults.joinToString("\n\n") { it.message }
                    "Categoría: $category\nPuntuación: $score\n$combinedMessage"
                } else {
                    "Categoría: $category\nPuntuación: $score\nResultado no encontrado."
                }
            }

            val finalResultMessage = if (resultMessages.isNotEmpty()) {
                resultMessages.joinToString("\n\n")
            } else {
                "No se pudo calcular el resultado."
            }

            guardarYMostrarResultado(finalResultMessage)
        }
    }
    private fun guardarYMostrarResultado(finalResultMessage: String) {

        lifecycleScope.launch(Dispatchers.IO) {
            try {
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

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("TEST_JSON", Gson().toJson(test))
            putExtra("USER_RESPONSES", Gson().toJson(userResponses))
        }

        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    startActivity(intent)
                    finish()
                    mInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    startActivity(intent)
                    finish()
                    mInterstitialAd = null
                }
            }
            mInterstitialAd?.show(this)
        } else {
            startActivity(intent)
            finish()
        }
    }

    private fun updateSelectedOptionUI(selectedIndex: Int, optionLayouts: List<LinearLayout>) {
        optionLayouts.forEachIndexed { index, layout ->
            if (index == selectedIndex) {
                // Aplicar fondo seleccionado
                layout.setBackgroundResource(R.drawable.selected_option_background)
            } else {
                // Fondo normal
                layout.setBackgroundResource(R.drawable.normal_option_background)
            }
        }
    }
    private fun startResultActivity() {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("TEST_JSON", Gson().toJson(test))
            putExtra("USER_RESPONSES", Gson().toJson(userResponses))
        }
        startActivity(intent)
        finish()
    }
}