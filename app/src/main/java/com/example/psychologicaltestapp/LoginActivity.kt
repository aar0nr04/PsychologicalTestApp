package com.example.psychologicaltestapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.psychologicaltestapp.MainActivity
import com.example.psychologicaltestapp.R
import com.example.psychologicaltestapp.RegisterActivity
import com.example.psychologicaltestapp.databinding.ActivityLoginBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var googleClient: GoogleSignInClient? = null

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: Exception) {
            showMsg("Google Sign-In cancelado o fallido: ${e.localizedMessage}")
            setLoading(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("es") // emails de Firebase en español

        // Ads
        loadBannerAd()

        // Google Sign-In
        setupGoogle()

        // Listeners
        binding.loginButton.setOnClickListener { loginWithEmail() }
        binding.googleSignInButton.setOnClickListener { signInWithGoogle() }
        binding.createAccountButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        // tvForgot puede no estar en el binding si el id no fue visto por el generador
        binding.root.findViewById<TextView>(R.id.tvForgot)?.setOnClickListener { sendResetEmail() }
        binding.backButton.setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        auth.currentUser?.let {
            navigateToHome(showWelcome = false)
        }
    }

    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        // Por si en algún layout no existe el AdView:
        binding.adView.loadAd(adRequest)
    }

    private fun setupGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        setLoading(true)
        googleClient?.let { client ->
            client.signOut() // forzar selector de cuenta
            // Aquí ya NO pasamos Intent?; es Intent no-nulo
            googleLauncher.launch(client.signInIntent)
        } ?: run {
            setLoading(false)
            showMsg("No se pudo inicializar Google Sign-In")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    onLoginSuccess()
                } else {
                    showMsg("Error al iniciar con Google: ${task.exception?.localizedMessage}")
                }
            }
    }

    private fun loginWithEmail() {
        val email = binding.emailEditText.text?.toString()?.trim().orEmpty()
        val pass = binding.passwordEditText.text?.toString().orEmpty()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMsg("Correo inválido")
            return
        }
        if (pass.length < 6) {
            showMsg("La contraseña debe tener al menos 6 caracteres")
            return
        }

        setLoading(true)
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user?.isEmailVerified == true) {
                        onLoginSuccess()
                    } else {
                        showMsg("Verifica tu correo para continuar")
                        user?.sendEmailVerification()
                    }
                } else {
                    showMsg(task.exception?.localizedMessage ?: "Error al iniciar sesión")
                }
            }
    }

    private fun sendResetEmail() {
        val email = binding.emailEditText.text?.toString()?.trim().orEmpty()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMsg("Escribe tu correo en el campo de correo para enviar el enlace")
            return
        }
        setLoading(true)
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    showMsg("Te enviamos un enlace para restablecer tu contraseña")
                } else {
                    showMsg("No pudimos enviar el correo: ${task.exception?.localizedMessage}")
                }
            }
    }

    private fun onLoginSuccess() {
        navigateToHome(showWelcome = true)
    }

    private fun navigateToHome(showWelcome: Boolean) {
        if (showWelcome) {
            showMsg("¡Bienvenido!")
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        val views = listOf(
            binding.loginButton,
            binding.googleSignInButton,
            binding.emailEditText,
            binding.passwordEditText,
            binding.backButton,
            binding.createAccountButton
        )
        views.forEach { it.isEnabled = !loading }
        // Si agregas ProgressBar en el XML, muéstralo/ocúltalo aquí
        // binding.progress.isVisible = loading
    }

    private fun showMsg(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
