package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VerifyEmailActivity : BaseActivity() {

    private val repo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)

        findViewById<Button>(R.id.btnResend).setOnClickListener {
            repo.resendVerification(
                onDone = { toast("Correo reenviado") },
                onError = { toast(it) }
            )
        }

        findViewById<Button>(R.id.btnVerified).setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
                val verified = FirebaseAuth.getInstance().currentUser?.isEmailVerified == true
                if (verified) {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    Firebase.firestore.collection("users").document(uid)
                        .update("verifiedEmail", true)
                    toast("Correo verificado")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    toast("Aún no aparece verificado")
                }
            }
        }
    }

    private fun toast(m: String) =
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
