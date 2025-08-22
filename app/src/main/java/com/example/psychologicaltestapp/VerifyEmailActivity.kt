package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VerifyEmailActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)

        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        findViewById<TextView>(R.id.tvInfo).text =
            "Te enviamos un correo de verificación. Revísalo y luego toca 'Ya verifiqué'."

        findViewById<Button>(R.id.btnResend).setOnClickListener {
            auth.currentUser?.sendEmailVerification()
            Toast.makeText(this, "Correo reenviado", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnVerified).setOnClickListener {
            auth.currentUser?.reload()?.addOnCompleteListener {
                val isVerified = auth.currentUser?.isEmailVerified == true
                if (isVerified) {
                    // Marca el flag en Firestore (opcional pero útil)
                    val uid = auth.currentUser!!.uid
                    db.collection("users").document(uid)
                        .update("verifiedEmail", true)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Aún no aparece verificado. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
