package com.example.psychologicaltestapp


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Reference buttons
        val loginButton = findViewById<Button>(R.id.loginButton)
        val testsButton = findViewById<Button>(R.id.testsButton)
        val psychologistDirectoryButton = findViewById<Button>(R.id.psychologistDirectoryButton)

        // Check if user is already logged in
        if (auth.currentUser != null) {
            // User is logged in, enable all options
            loginButton.text = "Cerrar sesión"
            loginButton.setOnClickListener {
                auth.signOut()
                recreate() // Refresh activity to update UI
            }
        } else {
            // User is not logged in, restrict access to tests and directory
            testsButton.isEnabled = true
            psychologistDirectoryButton.isEnabled = true

            loginButton.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        // Navigate to Tests screen
        testsButton.setOnClickListener {
            startActivity(Intent(this, TestListActivity::class.java))
        }

        // Navigate to Psychologist Directory screen
        psychologistDirectoryButton.setOnClickListener {
            startActivity(Intent(this, PsychologistDirectoryActivity::class.java))
        }
    }
}