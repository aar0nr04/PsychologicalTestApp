package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : BaseActivity() {

    private lateinit var authRepository: AuthRepository
    private var isPsychologist = false
    private var termsAccepted = false

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

        isPsychologist = intent.getBooleanExtra("registerAsPsychologist", false)
        updateRoleUi(toggleRoleButton, psychologistFields)

        registerButton.isEnabled = false
        updateTermsUi(acceptTermsButton, registerButton)
        acceptTermsButton.setOnClickListener {
            showTermsDialog(onAccepted = {
                termsAccepted = true
                updateTermsUi(acceptTermsButton, registerButton)
            })
        }

        toggleRoleButton.setOnClickListener {
            isPsychologist = !isPsychologist
            updateRoleUi(toggleRoleButton, psychologistFields)
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val pass = passwordEditText.text.toString().trim()

            if (!termsAccepted) return@setOnClickListener toast(getString(R.string.register_terms_required))
            if (name.isEmpty()) return@setOnClickListener toast(getString(R.string.register_name_required))
            if (email.isEmpty() || pass.length < 6) return@setOnClickListener toast(getString(R.string.register_email_password_required))

            val role = if (isPsychologist) "psychologist" else "patient"
            var specialty: String? = null
            var license: String? = null
            var phone: String? = null
            var about: String? = null

            if (isPsychologist) {
                specialty = editSpecialty.text?.toString()?.trim()
                license = editLicense.text?.toString()?.trim()
                phone = editPhone.text?.toString()?.trim()
                about = editAbout.text?.toString()?.trim()

                if (specialty.isNullOrEmpty() || license.isNullOrEmpty() || phone.isNullOrEmpty() || about.isNullOrEmpty()) {
                    toast(getString(R.string.register_psychologist_required))
                    return@setOnClickListener
                }
            }

            authRepository.registerUser(
                email = email,
                password = pass,
                role = role,
                onSuccess = {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val updates = mutableMapOf<String, Any>(
                        "displayName" to name,
                        "role" to role,
                        "verifiedEmail" to false,
                        "termsAccepted" to true,
                        "termsAcceptedAt" to FieldValue.serverTimestamp()
                    )
                    if (isPsychologist) {
                        updates["specialty"] = specialty!!
                        updates["license"] = license!!
                        updates["phone"] = phone!!
                        updates["about"] = about!!
                        updates["isApprovedPsychologist"] = false
                    }
                    Firebase.firestore.collection("users").document(uid)
                        .set(updates, SetOptions.merge())

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

    private fun showTermsDialog(onAccepted: () -> Unit) {
        val termsText = runCatching {
            assets.open("legal/terms_es.md").bufferedReader().use { it.readText() }
        }.getOrElse {
            getString(R.string.register_terms_unavailable)
        }

        val displayText = termsText.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")

        AlertDialog.Builder(this)
            .setTitle(R.string.register_terms_title)
            .setMessage(displayText)
            .setPositiveButton(R.string.register_terms_accept) { _, _ -> onAccepted() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateRoleUi(toggleButton: Button, psychologistFields: LinearLayout) {
        psychologistFields.visibility = if (isPsychologist) View.VISIBLE else View.GONE
        toggleButton.text = if (isPsychologist) {
            getString(R.string.register_switch_to_patient)
        } else {
            getString(R.string.register_switch_to_psychologist)
        }
    }

    private fun updateTermsUi(acceptButton: Button, registerButton: Button) {
        acceptButton.text = if (termsAccepted) {
            getString(R.string.register_terms_accepted)
        } else {
            getString(R.string.register_terms_review)
        }
        registerButton.isEnabled = termsAccepted
    }

    private fun toast(m: String) =
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
