package com.example.psychologicaltestapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.util.Log

class AiTipsActivity : BaseActivity() {

    private lateinit var aiTipsTextView: TextView
    private lateinit var regenerateButton: Button
    private lateinit var backButton: Button

    private val client = OkHttpClient()

    // ⚠️ REEMPLAZA ESTA IP POR LA DE TU PC EN LA RED LOCAL
    private val backendUrl = "http://192.168.1.64:3000/generateTips"

    private var currentPrompt: String = "Dame consejos para sentirme mejor."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ai_tips_activity)

        aiTipsTextView = findViewById(R.id.aiTipsTextView)
        regenerateButton = findViewById(R.id.regenerateButton)
        backButton = findViewById(R.id.backButton)

        currentPrompt = intent.getStringExtra("prompt") ?: currentPrompt

        obtenerTips(currentPrompt)

        regenerateButton.setOnClickListener {
            obtenerTips(currentPrompt)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun obtenerTips(prompt: String) {
        runOnUiThread {
            aiTipsTextView.text = "Generando recomendaciones..."
        }

        val json = JSONObject()
        json.put("prompt", prompt)

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(), json.toString()
        )

        val request = Request.Builder()
            .url(backendUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    aiTipsTextView.text = "Error de red: ${e.message}"
                    Log.e("AiTipsActivity", "Error de red", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            aiTipsTextView.text = "Error en servidor: ${response.code}"
                            Log.e("AiTipsActivity", "Error en servidor: ${response.code}")
                        }
                    } else {
                        val responseData = response.body?.string()
                        Log.d("AiTipsActivity", "Respuesta backend: $responseData")
                        try {
                            val jsonResponse = JSONObject(responseData)
                            val tips = jsonResponse.optString("tips", "No se recibieron recomendaciones.")
                            runOnUiThread {
                                aiTipsTextView.text = tips
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                aiTipsTextView.text = "Error parseando respuesta."
                                Log.e("AiTipsActivity", "JSON parse error", e)
                            }
                        }
                    }
                }
            }
        })
    }
}
