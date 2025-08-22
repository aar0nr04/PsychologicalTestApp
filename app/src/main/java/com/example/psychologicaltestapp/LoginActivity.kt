package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ApiException

class LoginActivity : BaseActivity() {

    private lateinit var authRepository: AuthRepository

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val token = account.idToken
            if (token == null) {
                Toast.makeText(this, "idToken nulo (revisa default_web_client_id y SHA en Firebase)", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            authRepository.signInWithGoogle(
                idToken = token,
                onSuccess = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onError = { err ->
                    android.util.Log.e("Auth", "Firebase signInWithCredential error: $err")
                    Toast.makeText(this, "Firebase Auth falló: $err", Toast.LENGTH_LONG).show()
                }
            )
        } catch (e: com.google.android.gms.common.api.ApiException) {
            android.util.Log.e("Auth", "Google Sign-In ApiException code=${e.statusCode}, msg=${e.message}", e)
            val human = when (e.statusCode) {
                7    -> "NETWORK_ERROR: Sin red o inestable."
                10   -> "DEVELOPER_ERROR: SHA-1/SHA-256 mal configurados o clientId incorrecto."
                12500-> "SIGN_IN_FAILED: Configuración OAuth/Firebase incompleta (SHA/ID)."
                12501-> "SIGN_IN_CANCELLED: el usuario canceló."
                12502-> "SIGN_IN_CURRENTLY_IN_PROGRESS."
                else -> "Error Google Sign-In (${e.statusCode})"
            }
            Toast.makeText(this, human, Toast.LENGTH_LONG).show()
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

        // Opcional: botón Google en tu layout con id googleSignInButton
        findViewById<Button?>(R.id.googleSignInButton)?.setOnClickListener {
            val client = authRepository.getGoogleClient(this)
            googleLauncher.launch(client.signInIntent)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                return@setOnClickListener toast("Completa todos los campos")
            }

            authRepository.loginUser(email, password,
                onSuccess = {
                    toast("Inicio de sesión exitoso")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onError = { errorMessage ->
                    // si no verificado, te manda el mensaje y ya reenvió correo
                    toast(errorMessage)
                    // si quieres mandar a VerifyEmailActivity:
                    // startActivity(Intent(this, VerifyEmailActivity::class.java))
                }
            )
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
