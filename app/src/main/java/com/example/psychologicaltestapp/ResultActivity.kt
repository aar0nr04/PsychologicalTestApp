package com.example.psychologicaltestapp

import UserRepository
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Toast
import com.example.psychologicaltestapp.databinding.ActivityResultBinding
import com.example.psychologicaltestapp.data.tests.TestPayload
import com.example.psychologicaltestapp.utils.Dialogs
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.util.Date

class ResultActivity : BaseActivity() {

    private lateinit var binding: ActivityResultBinding

    // Nuevo payload unificado
    private lateinit var payload: TestPayload
    private var userResponses: List<Int?> = emptyList()

    // Texto final mostrado
    private var finalResultMessage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ads
        MobileAds.initialize(this) {}
        binding.adView.loadAd(AdRequest.Builder().build())

        // ----- Recuperar datos del Intent -----
        val payloadJson = intent.getStringExtra("TEST_PAYLOAD")
        val responsesJson = intent.getStringExtra("USER_RESPONSES")
        val finalMsgFromRunner = intent.getStringExtra("FINAL_MESSAGE")

        if (payloadJson.isNullOrBlank() || responsesJson.isNullOrBlank()) {
            Toast.makeText(this, "Datos del test incompletos.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        try {
            payload = Gson().fromJson(payloadJson, TestPayload::class.java)
            userResponses = Gson().fromJson(responsesJson, Array<Int?>::class.java).toList()
        } catch (e: JsonSyntaxException) {
            Toast.makeText(this, "Error interpretando datos del test.", Toast.LENGTH_LONG).show()
            finish()
            return
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Título en el encabezado
        binding.ResultadoTitle.text = "Resultado — ${payload.title}"

        // Si el Runner ya mandó el mensaje final, úsalo; si no, lo calculamos aquí
        finalResultMessage = if (!finalMsgFromRunner.isNullOrBlank()) {
            finalMsgFromRunner
        } else {
            buildResultText(calculateScaleTotals())
        }

        // Mostrar resultado
        binding.resultTextView.text = finalResultMessage

        // Guardar en Firestore (si hay usuario)
        saveResultToFirestore()

        // Botones
        binding.aiTipsButton.setOnClickListener {
            val tips = generateAiLikeTips(finalResultMessage)
            Dialogs.simpleMessage(this, "Sugerencias", tips).show()
        }

        binding.psychologistButton.setOnClickListener {
            // Abre tu directorio si lo tienes; si no, muestra aviso
            try {
                startActivity(Intent(this, PsychologistDirectoryActivity::class.java))
            } catch (_: Exception) {
                Toast.makeText(this, "Directorio no disponible aún.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backToMenuButton.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(i)
            finish()
        }
    }

    // ---------------- Cálculo de escalas ----------------

    private fun calculateScaleTotals(): Map<String, Int> {
        val totals = mutableMapOf<String, Int>()
        payload.scales.forEach { totals[it.id] = 0 }

        payload.questions.forEachIndexed { idx, q ->
            val selected = userResponses.getOrNull(idx) ?: return@forEachIndexed

            // a) Si hay matriz por escala/opción (AMAS-A), úsala
            if (q.scoresMatrix != null) {
                q.scoresMatrix.forEach { (scaleId, weights) ->
                    val add = weights.getOrNull(selected) ?: 0
                    totals[scaleId] = (totals[scaleId] ?: 0) + add
                }
            } else {
                // b) Fallback: option.value (o índice) + scores (método general)
                val optValue = q.options?.getOrNull(selected)?.value ?: selected
                q.scores?.forEach { s ->
                    val add = when (s.mapping) {
                        null, "", "option.value" -> optValue
                        else -> optValue
                    }
                    totals[s.scale] = (totals[s.scale] ?: 0) + add
                }
            }
        }
        return totals
    }

    private fun buildResultText(scaleTotals: Map<String, Int>): String {
        val sb = StringBuilder()
        payload.results.scales.forEach { rs ->
            val total = scaleTotals[rs.id] ?: 0
            val range = rs.ranges.firstOrNull { total in it.min..it.max }
            sb.append("Escala: ${labelForScale(rs.id)}\n")
            sb.append("Puntaje: $total\n")
            if (range != null) {
                sb.append("Nivel: ${range.label}\n")
                range.advice?.let { sb.append("Consejo: $it\n") }
            } else {
                sb.append("Nivel: (sin rango definido)\n")
            }
            sb.append("\n")
        }
        return sb.toString().trim()
    }

    private fun labelForScale(id: String): String {
        // Busca un título formal si existe
        val inPayload = payload.scales.firstOrNull { it.id == id }?.title
        return inPayload ?: id
    }

    // ---------------- Persistencia ----------------

    private fun saveResultToFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        // Re-armar un objeto similar al que ya almacenabas
        val testResult = TestResult(
            testType = payload.slug,                 // antes: test.type
            testName = payload.title,               // antes: test.title
            resultMessage = finalResultMessage,
            date = DateFormat.format("yyyy-MM-dd HH:mm:ss", Date()).toString(),
            testJson = Gson().toJson(payload),      // guarda el payload usado
            userResponsesJson = Gson().toJson(userResponses),
            userId = currentUser.uid,
            createdAt = Timestamp.now()
        )

        UserRepository().saveTestResult(currentUser.uid, testResult)
    }

    // ---------------- Utilidades UI ----------------

    private fun generateAiLikeTips(resultText: String): String {
        // Placeholder local (hasta que conectes tu backend/LLM)
        // Saca 3 bullets amigables en base al texto de resultados
        val base = """
            Basado en tus resultados:
            • Anota (durante 7 días) situaciones que disparen tensión/ansiedad; identifica patrones.
            • Practica respiración diafragmática 5 minutos, 2 veces al día.
            • Define una micro-acción diaria (5–10 min) para avanzar un objetivo personal.
        """.trimIndent()
        return base
    }
}
