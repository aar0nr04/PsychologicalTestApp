package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar repositorios
        authRepository = AuthRepository()
        userRepository = UserRepository()

        // Referencias a los elementos de la interfaz
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        // Configurar el clic del botón de registro
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()

            // Validar que los campos no estén vacíos
            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Registrar al usuario
            authRepository.registerUser(email, password,
                onSuccess = {
                    // Obtener el ID del usuario registrado
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    userId?.let {
                        // Guardar datos adicionales del usuario en Firestore
                        userRepository.saveUserData(it, email, name)
                    }
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                    // Redirigir al usuario a la pantalla principal o perfil
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Cierra la pantalla de registro
                },
                onError = { errorMessage ->
                    // Mostrar un mensaje de error si falla el registro
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}