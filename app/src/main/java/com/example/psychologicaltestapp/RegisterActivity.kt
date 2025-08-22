package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class RegisterActivity : BaseActivity() {

    private lateinit var authRepository: AuthRepository
    private var isPsychologist = false
    private var termsAccepted = true // si quieres forzar aceptación, cámbialo a false y controla el botón

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authRepository = AuthRepository()

        // Views principales
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val toggleRoleButton = findViewById<Button>(R.id.toggleRoleButton)
        val psychologistFields = findViewById<LinearLayout>(R.id.psychologistFields)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val acceptTermsButton = findViewById<Button>(R.id.acceptTermsButton)

        // Campos extra (solo si isPsychologist == true)
        val editSpecialty = findViewById<EditText>(R.id.editSpecialty)
        val editLicense   = findViewById<EditText>(R.id.editLicense)
        val editPhone     = findViewById<EditText>(R.id.editPhone)
        val editAbout     = findViewById<EditText>(R.id.editAbout)

        // (Opcional) Aceptación de términos
        acceptTermsButton.setOnClickListener {
            termsAccepted = true
            Toast.makeText(this, "Términos aceptados", Toast.LENGTH_SHORT).show()
            // registerButton.isEnabled = true // si lo quieres habilitar aquí
        }

        // Alternar rol
        toggleRoleButton.setOnClickListener {
            isPsychologist = !isPsychologist
            psychologistFields.visibility = if (isPsychologist) View.VISIBLE else View.GONE
            toggleRoleButton.text = if (isPsychologist)
                "Registrarse como Paciente" else "Registrarse como Psicólogo"
        }

        // Registrar
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val pass = passwordEditText.text.toString().trim()

            if (!termsAccepted) {
                Toast.makeText(this, "Debes aceptar los términos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty() || pass.length < 6) {
                Toast.makeText(this, "Revisa email y contraseña (mín. 6)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val role = if (isPsychologist) "psychologist" else "patient"

            authRepository.registerUser(
                email = email,
                password = pass,
                role = role,
                onSuccess = {
                    // Guarda extras (opcional)
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val updates = mutableMapOf<String, Any>(
                        "displayName" to name,
                        "role" to role,
                        "verifiedEmail" to false
                    )
                    if (isPsychologist) {
                        editSpecialty.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { updates["specialty"] = it }
                        editLicense.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { updates["license"] = it }
                        editPhone.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { updates["phone"] = it }
                        editAbout.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { updates["about"] = it }
                        updates["isApprovedPsychologist"] = false
                    }
                    Firebase.firestore.collection("users").document(uid).update(updates)

                    Toast.makeText(this, "Cuenta creada. Te enviamos verificación.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, VerifyEmailActivity::class.java))
                    finish()
                },
                onError = { err ->
                    Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                }
            )
        }

        // (Opcional) Cargar banner de Ads si lo usas
        findViewById<AdView?>(R.id.adView)?.let { adView ->
            MobileAds.initialize(this) {}
            adView.loadAd(AdRequest.Builder().build())
        }
    }
}
