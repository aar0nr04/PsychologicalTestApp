package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var testHistoryContainer: LinearLayout
    private lateinit var logoutButton: Button

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImage = findViewById(R.id.profileImage)
        profileName = findViewById(R.id.profileName)
        profileEmail = findViewById(R.id.profileEmail)
        testHistoryContainer = findViewById(R.id.testHistoryContainer)
        logoutButton = findViewById(R.id.logoutButton)

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        profileName.text = user.displayName ?: "Nombre no disponible"
        profileEmail.text = user.email ?: "Correo no disponible"

        user.photoUrl?.let {
            Glide.with(this).load(it).into(profileImage)
        }

        loadTestHistory(user.uid)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadTestHistory(userId: String) {
        val firestore = Firebase.firestore
        firestore.collection("users")
            .document(userId)
            .collection("test_results")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    val emptyView = TextView(this)
                    emptyView.text = "No has realizado ningún test aún."
                    testHistoryContainer.addView(emptyView)
                } else {
                    for (doc in result.documents) {
                        val testName = doc.getString("testName") ?: "Test"
                        val date = doc.getString("date") ?: ""
                        val view = TextView(this)
                        view.text = "🧠 $testName\n📅 $date"
                        view.setPadding(8, 8, 8, 16)
                        testHistoryContainer.addView(view)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar historial", Toast.LENGTH_SHORT).show()
            }
    }
}
