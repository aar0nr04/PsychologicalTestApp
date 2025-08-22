package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : BaseActivity() {

    private lateinit var authRepository: AuthRepository
    private var isPsychologist = false
    private var termsAccepted = true // cambia a false si quieres forzar aceptación

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authRepository = AuthRepository()

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val toggleRoleButton = findViewById<Button>(R.id.toggleRoleButton)
        val psychologistFields = findViewById<LinearLayout>(R.id.psychologistFields)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val acceptTermsButton = findViewById<Button>(R.id.acceptTermsButton)

        val editSpecialty = findViewById<EditText>(R.id.editSpecialty)
        val editLicense   = findViewById<EditText>(R.id.editLicense)
        val editPhone     = findViewById<EditText>(R.id.editPhone)
        val editAbout     = findViewById<EditText>(R.id.editAbout)

        acceptTermsButton.setOnClickListener {
            termsAccepted = true
            Toast.makeText(this, "Términos aceptados", Toast.LENGTH_SHORT).show()
        }

        toggleRoleButton.setOnClickListener {
            isPsychologist = !isPsychologist
            psychologistFields.visibility = if (isPsychologist) View.VISIBLE else View.GONE
            toggleRoleButton.text = if (isPsychologist)
                "Registrarse como Paciente" else "Registrarse como Psicólogo"
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val pass = passwordEditText.text.toString().trim()

            if (!termsAccepted) return@setOnClickListener toast("Debes aceptar los términos")
            if (email.isEmpty() || pass.length < 6) return@setOnClickListener toast("Revisa email y contraseña (mín. 6)")

            val role = if (isPsychologist) "psychologist" else "patient"

            authRepository.registerUser(
                email = email,
                password = pass,
                role = role,
                onSuccess = {
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

                    toast("Cuenta creada. Te enviamos verificación.")
                    startActivity(Intent(this, VerifyEmailActivity::class.java))
                    finish()
                },
                onError = { err -> toast("Error: $err") }
            )
        }

        // (Opcional) Ads banner si lo usas en esta pantalla
        findViewById<AdView?>(R.id.adView)?.let { adView ->
            MobileAds.initialize(this) {}
            adView.loadAd(AdRequest.Builder().build())
        }
    }

    private fun toast(m: String) =
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
