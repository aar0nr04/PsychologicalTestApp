package com.example.psychologicaltestapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiTipsActivity : BaseActivity() {

    private lateinit var aiTipsTextView: TextView
    private lateinit var regenerateButton: Button
    private lateinit var backButton: Button
    private lateinit var progress: ProgressBar

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val backendUrl: String = BuildConfig.AI_TIPS_BASE_URL

    private var currentPrompt: String = "Dame consejos para sentirme mejor."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ai_tips_activity)

        aiTipsTextView = findViewById(R.id.aiTipsTextView)
        regenerateButton = findViewById(R.id.regenerateButton)
        backButton = findViewById(R.id.backButton)
        progress = findViewById(R.id.progressBar)

        currentPrompt = intent.getStringExtra("prompt") ?: currentPrompt

        if (backendUrl.isBlank()) {
            aiTipsTextView.text = getString(R.string.ai_tips_backend_missing)
            regenerateButton.isEnabled = false
        } else {
            fetchTips(currentPrompt)
            regenerateButton.setOnClickListener { fetchTips(currentPrompt) }
        }

        backButton.setOnClickListener { finish() }
    }

    private fun fetchTips(prompt: String) {
        showLoading(true)

        val body = RequestBody.create(JSON_MEDIA_TYPE, JSONObject().apply {
            put("prompt", prompt)
        }.toString())

        val request = Request.Builder()
            .url(backendUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showLoading(false)
                    aiTipsTextView.text = getString(R.string.ai_tips_network_error, e.localizedMessage ?: "")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val bodyString = response.body?.string()
                    runOnUiThread {
                        showLoading(false)
                        if (!response.isSuccessful || bodyString == null) {
                            aiTipsTextView.text = getString(R.string.ai_tips_server_error, response.code)
                            return@runOnUiThread
                        }

                        val tips = runCatching {
                            val jsonResponse = JSONObject(bodyString)
                            jsonResponse.optString("tips")
                        }.getOrElse {
                            ""
                        }

                        aiTipsTextView.text = if (tips.isNullOrBlank()) {
                            getString(R.string.ai_tips_empty_response)
                        } else {
                            tips
                        }
                    }
                }
            }
        })
    }

    private fun showLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        regenerateButton.isEnabled = !loading
        if (loading) {
            aiTipsTextView.text = getString(R.string.ai_tips_generating)
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
