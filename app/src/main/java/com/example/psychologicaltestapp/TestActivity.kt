package com.example.psychologicaltestapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.psychologicaltestapp.data.tests.TestPayload
import com.example.psychologicaltestapp.data.tests.TestsRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson

class TestActivity : AppCompatActivity() {

    // --- Ads ---
    private var mInterstitialAd: InterstitialAd? = null

    // --- UI ---
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var progressTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var questionImage: ImageView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var optionsGrid: GridLayout

    // --- Test state ---
    private lateinit var testPayload: TestPayload
    private var currentQuestionIndex = 0
    private var selectedOptionIndex: Int? = null
    private var currentQuestionIsRequired: Boolean = true
    private val userResponses: MutableList<Int?> = mutableListOf()

    // --- Locale para títulos/opciones (si algún test lo requiere) ---
    private val locale = "es"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // AdMob (banner + intersticial)
        MobileAds.initialize(this) {}
        val adView = findViewById<AdView>(R.id.adView)
        adView?.loadAd(AdRequest.Builder().build())
        loadInterstitialAd()

        // Bind views
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)
        nextButton = findViewById(R.id.nextButton)
        progressTextView = findViewById(R.id.progressTextView)
        questionTextView = findViewById(R.id.questionTextView)
        questionImage = findViewById(R.id.questionImage)
        optionsContainer = findViewById(R.id.optionsContainer)
        optionsGrid = findViewById(R.id.optionsGrid)

        nextButton.isEnabled = false

        // Lee parámetros desde el intent
        val slug = intent.getStringExtra("slug")
        val version = intent.getStringExtra("version")
        val localExtra = intent.getStringExtra("locale") ?: locale

        if (slug.isNullOrBlank() || version.isNullOrBlank()) {
            Toast.makeText(this, "Faltan datos del test.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Carga el payload del test
        try {
            val json = TestsRepository.loadTestPayload(this, slug, version, localExtra)
            testPayload = Gson().fromJson(json, TestPayload::class.java)
        } catch (e: Exception) {
            Toast.makeText(this, "Error cargando test: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Prepara estado
        userResponses.clear()
        userResponses.addAll(List(testPayload.questions.size) { null })

        // UI inicial
        findViewById<TextView?>(R.id.testTitle)?.text = testPayload.title

        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        nextButton.setOnClickListener { handleNext() }

        showQuestion()
    }

    // ---------------- Ads ----------------
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            getString(R.string.admob_interstitial_id), // Asegúrate de tener este string en resources
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    mInterstitialAd = ad
                    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            startResultActivity()
                            mInterstitialAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            startResultActivity()
                            mInterstitialAd = null
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d("TestActivity", "Interstitial failed: ${error.message}")
                    mInterstitialAd = null
                }
            }
        )
    }

    private fun showInterstitialOrResults() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            startResultActivity()
        }
    }

    // --------------- Navegación Preguntas ---------------
    private fun handleNext() {
        if (currentQuestionIsRequired && selectedOptionIndex == null) {
            Toast.makeText(this, R.string.test_required_question_warning, Toast.LENGTH_SHORT).show()
            return
        }

        // Guarda la respuesta actual (incluye null si se permite omitir)
        userResponses[currentQuestionIndex] = selectedOptionIndex

        if (currentQuestionIndex < testPayload.questions.size - 1) {
            currentQuestionIndex++
            selectedOptionIndex = userResponses[currentQuestionIndex]
            showQuestion()
        } else {
            // Última pregunta → calcular resultado y mostrar (con intersticial)
            showResult()
        }
    }

    private fun showQuestion() {
        if (currentQuestionIndex >= testPayload.questions.size) {
            showResult()
            return
        }

        val q = testPayload.questions[currentQuestionIndex]
        currentQuestionIsRequired = q.required != false

        // Prompt
        if (!q.prompt.isNullOrEmpty()) {
            questionTextView.text = q.prompt
            questionTextView.visibility = View.VISIBLE
        } else {
            questionTextView.visibility = View.GONE
        }

        // Imagen de la pregunta (si la usas en tu JSON)
        if (!q.imageQuestion.isNullOrEmpty()) {
            try {
                val bmp = assets.open(q.imageQuestion!!).use { BitmapFactory.decodeStream(it) }
                questionImage.setImageBitmap(bmp)
                questionImage.visibility = View.VISIBLE
            } catch (_: Exception) {
                questionImage.visibility = View.GONE
            }
        } else {
            questionImage.visibility = View.GONE
        }

        // Render de opciones:
        // Si tienes optionImages, usa el Grid; si no, usa botones verticales
        optionsContainer.removeAllViews()
        optionsGrid.removeAllViews()

        val hasOptionImages = (q.optionImages?.isNotEmpty() == true)
        val opts = q.options ?: emptyList()

        if (hasOptionImages) {
            // GRID de imágenes + label
            optionsGrid.visibility = View.VISIBLE
            optionsContainer.visibility = View.GONE
            optionsGrid.columnCount = 2

            opts.forEachIndexed { idx, opt ->
                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(8, 8, 8, 8)
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.MarginLayoutParams.MATCH_PARENT,
                        ViewGroup.MarginLayoutParams.WRAP_CONTENT
                    )
                    setBackgroundResource(R.drawable.normal_option_background)
                }

                // Imagen
                val imgPath = q.optionImages?.getOrNull(idx)
                if (!imgPath.isNullOrEmpty()) {
                    try {
                        val imageView = ImageView(this).apply {
                            val bmp = assets.open(imgPath).use { BitmapFactory.decodeStream(it) }
                            setImageBitmap(bmp)
                            adjustViewBounds = true
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                300
                            )
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                        layout.addView(imageView)
                    } catch (_: Exception) {
                        // ignora si no encuentra la imagen
                    }
                }

                // Label
                val tv = TextView(this).apply {
                    text = opt.label
                    gravity = Gravity.CENTER
                    setPadding(8, 12, 8, 12)
                }
                layout.addView(tv)

                layout.setOnClickListener {
                    onOptionSelected(idx)
                    updateGridSelection(optionsGrid, idx)
                }

                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                layout.layoutParams = params
                optionsGrid.addView(layout)
            }

            // Restaurar selección previa
            selectedOptionIndex = userResponses[currentQuestionIndex]
            selectedOptionIndex?.let { updateGridSelection(optionsGrid, it) }
            nextButton.isEnabled = !currentQuestionIsRequired || selectedOptionIndex != null

        } else {
            // LISTA de botones (texto)
            optionsGrid.visibility = View.GONE
            optionsContainer.visibility = View.VISIBLE

            opts.forEachIndexed { idx, opt ->
                val btn = Button(this).apply {
                    text = opt.label
                    setBackgroundResource(R.drawable.custom_button_style)
                    setOnClickListener {
                        onOptionSelected(idx)
                        updateListSelection(optionsContainer, idx)
                    }
                }
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 8, 0, 8) }
                optionsContainer.addView(btn, lp)
            }

            // Restaurar selección previa
            selectedOptionIndex = userResponses[currentQuestionIndex]
            selectedOptionIndex?.let { updateListSelection(optionsContainer, it) }
            nextButton.isEnabled = !currentQuestionIsRequired || selectedOptionIndex != null
        }

        // Progreso
        val total = testPayload.questions.size
        val current = currentQuestionIndex + 1
        progressTextView.text = "Pregunta $current de $total"
        progressBar.max = total
        progressBar.progress = current
    }

    private fun onOptionSelected(idx: Int) {
        selectedOptionIndex = idx
        nextButton.isEnabled = true
        // Guarda de inmediato para evitar perder selección al rotar (si aplica)
        userResponses[currentQuestionIndex] = idx
    }

    private fun updateListSelection(container: LinearLayout, selectedIdx: Int) {
        for (i in 0 until container.childCount) {
            val v = container.getChildAt(i)
            if (v is Button) {
                v.setBackgroundResource(
                    if (i == selectedIdx) R.drawable.custom_button_selected
                    else R.drawable.custom_button_style
                )
            }
        }
    }

    private fun updateGridSelection(grid: GridLayout, selectedIdx: Int) {
        for (i in 0 until grid.childCount) {
            val v = grid.getChildAt(i)
            v.setBackgroundResource(
                if (i == selectedIdx) R.drawable.custom_button_selected
                else R.drawable.normal_option_background
            )
        }
    }

    // --------------- Scoring + Resultado ---------------
    private fun showResult() {
// 1) Sumar por escala
        val scaleTotals = mutableMapOf<String, Int>().apply {
            testPayload.scales.forEach { put(it.id, 0) }
        }

        testPayload.questions.forEachIndexed { idx, q ->
            val selected = userResponses[idx] ?: return@forEachIndexed

            // a) Si hay matriz por escala/opción, úsala
            if (q.scoresMatrix != null) {
                q.scoresMatrix.forEach { (scaleId, weights) ->
                    val add = weights.getOrNull(selected) ?: 0
                    scaleTotals[scaleId] = (scaleTotals[scaleId] ?: 0) + add
                }
            } else {
                // b) Fallback: option.value (o índice)
                val optValue = q.options?.getOrNull(selected)?.value ?: selected
                q.scores?.forEach { s ->
                    val scaleId = s.scale
                    val add = when (s.mapping) {
                        null, "", "option.value" -> optValue
                        else -> optValue
                    }
                    scaleTotals[scaleId] = (scaleTotals[scaleId] ?: 0) + add
                }
            }
        }

        // 2) Resolver rangos y construir mensaje
        val sb = StringBuilder()
        testPayload.results.scales.forEach { rs ->
            val total = scaleTotals[rs.id] ?: 0
            val match = rs.ranges.firstOrNull { total in it.min..it.max }
            if (match != null) {
                sb.append("Escala: ${rs.id}\n")
                sb.append("Puntaje: $total\n")
                sb.append("Nivel: ${match.label}\n")
                match.advice?.let { sb.append("Consejo: $it\n") }
                sb.append("\n")
            } else {
                sb.append("Escala: ${rs.id}\nPuntaje: $total\nSin rango definido.\n\n")
            }
        }
        val finalMessage = sb.toString().trim().ifEmpty { "Resultado listo." }

        // 3) Guarda y muestra: intersticial → ResultActivity
        guardarYMostrarResultado(finalMessage)
    }

    private fun guardarYMostrarResultado(finalMessage: String) {
        // Si necesitas guardar en Firestore, hazlo aquí (uid, slug, scores, etc.)
        // Para mantener compatibilidad con tu ResultActivity actual, enviamos el payload y respuestas.
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("TEST_PAYLOAD", Gson().toJson(testPayload))
            putExtra("USER_RESPONSES", Gson().toJson(userResponses))
            putExtra("FINAL_MESSAGE", finalMessage)
        }

        // Muestra intersticial si está listo; al cerrarse abre ResultActivity.
        if (mInterstitialAd != null) {
            // Al dismiss, el callback de fullScreenContent llama startResultActivity()
            // así que guardamos el intent en un campo temporal si lo prefieres.
            lastResultIntent = intent
            mInterstitialAd?.show(this)
        } else {
            startActivity(intent)
            finish()
        }
    }

    private var lastResultIntent: Intent? = null

    private fun startResultActivity() {
        val i = lastResultIntent ?: Intent(this, ResultActivity::class.java).apply {
            putExtra("TEST_PAYLOAD", Gson().toJson(testPayload))
            putExtra("USER_RESPONSES", Gson().toJson(userResponses))
        }
        startActivity(i)
        finish()
    }
}
