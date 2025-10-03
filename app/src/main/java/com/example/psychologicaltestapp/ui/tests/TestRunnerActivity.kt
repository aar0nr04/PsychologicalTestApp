package com.example.psychologicaltestapp.ui.tests

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.psychologicaltestapp.data.tests.TestsRepository
import com.example.psychologicaltestapp.databinding.ActivityTestRunnerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class TestRunnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestRunnerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestRunnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slug = intent.getStringExtra("slug")
        val version = intent.getStringExtra("version")
        val locale = intent.getStringExtra("locale") ?: "es"

        if (slug.isNullOrBlank() || version.isNullOrBlank()) {
            Toast.makeText(this, "Faltan datos del test.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Carga del payload en background
        lifecycleScope.launch {
            try {
                binding.stateText.text = "Cargando test..."
                val payload = withContext(Dispatchers.IO) {
                    TestsRepository.loadTestPayload(this@TestRunnerActivity, slug, version, locale)
                }

                // (simple) toma el título para mostrarlo; luego parseas a tu modelo completo
                val title = runCatching {
                    JSONObject(payload).optString("title", slug)
                }.getOrDefault(slug)

                binding.titleText.text = title
                binding.stateText.text = "Listo. ¡Comencemos!"
                // TODO: aquí inicia tu flujo de preguntas con el payload parseado

            } catch (e: Exception) {
                Toast.makeText(this@TestRunnerActivity, "Error cargando el test.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
