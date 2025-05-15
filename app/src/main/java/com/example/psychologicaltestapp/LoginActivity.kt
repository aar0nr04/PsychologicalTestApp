package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.psychologicaltestapp.AuthRepository

class LoginActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authRepository = AuthRepository()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val backButton = findViewById<Button>(R.id.backButton)

        // Handle login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authRepository.loginUser(email, password,
                    onSuccess = {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Close the LoginActivity
                    },
                    onError = { errorMessage ->
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle back button click
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the LoginActivity
        }
    }
}