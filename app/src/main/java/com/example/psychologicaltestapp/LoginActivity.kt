package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class LoginActivity : BaseActivity() {

    private lateinit var authRepository: AuthRepository

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            if (token == null) {
                toast("idToken nulo → revisa google-services.json / SHA")
                return@registerForActivityResult
            }
            authRepository.signInWithGoogle(
                idToken = token,
                onSuccess = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onError = { err -> toast("Google Auth falló: $err") }
            )
        } catch (e: ApiException) {
            toast("Google Sign-In error (${e.statusCode})")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authRepository = AuthRepository()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val backButton = findViewById<Button>(R.id.backButton)

        // Botón de Google si existe en tu XML
        findViewById<Button?>(R.id.googleSignInButton)?.setOnClickListener {
            val client = authRepository.getGoogleClient(this)
            client.signOut().addOnCompleteListener { // limpia estado viejo
                googleLauncher.launch(client.signInIntent)
            }
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) return@setOnClickListener toast("Completa todos los campos")

            authRepository.loginUser(email, password,
                onSuccess = {
                    toast("Inicio de sesión exitoso")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onError = { errorMessage -> toast(errorMessage) }
            )
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
