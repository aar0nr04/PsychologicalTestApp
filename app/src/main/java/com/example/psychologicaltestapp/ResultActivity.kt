package com.example.psychologicaltestapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.psychologicaltestapp.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout usando View Binding
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el mensaje de resultado del Intent
        val resultMessage = intent.getStringExtra("RESULT_MESSAGE") ?: "Resultado no disponible."

        // Mostrar el resultado en el TextView
        binding.resultTextView.text = resultMessage

        // Manejar el botón "Regresar al Menú Principal"
        binding.backToMenuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finaliza esta actividad para evitar que el usuario regrese aquí al presionar "Atrás"
        }
    }
}