package com.example.psychologicaltestapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.example.psychologicaltestapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

 //       setupVideoBackground()
        setupAdMob()
        setupButtons()
//        binding.videoBackground.setOnErrorListener { mediaPlayer, what, extra ->
//            println("MediaPlayer error in TestActivity: what=$what, extra=$extra")
//            true
//        }
        updateUI()
    }

//    private fun setupVideoBackground() {
//        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.clouds_video}")
//        binding.videoBackground.setVideoURI(videoUri)
//        binding.videoBackground.setOnPreparedListener { mediaPlayer ->
//            mediaPlayer.isLooping = true
//            mediaPlayer.setVolume(0f, 0f) // Mute the video
//            binding.videoBackground.start()
//        }
//    }

    private fun setupAdMob() {
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun setupButtons() {
        binding.loginButton.setOnClickListener {
            if (auth.currentUser != null) {
                auth.signOut()
                recreate()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        binding.testsButton.setOnClickListener {
            startActivity(Intent(this, TestListActivity::class.java))
        }

        binding.psychologistDirectoryButton.setOnClickListener {
            startActivity(Intent(this, PsychologistDirectoryActivity::class.java))
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun updateUI() {
        if (auth.currentUser != null) {
            binding.loginButton.text = "Cerrar sesión"
            binding.testsButton.isEnabled = true
            binding.psychologistDirectoryButton.isEnabled = true
        } else {
            binding.loginButton.text = "Iniciar sesión"
            binding.testsButton.isEnabled = true
            binding.psychologistDirectoryButton.isEnabled = true
        }
    }

//    override fun onPause() {
//        super.onPause()
//        binding.videoBackground.pause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        binding.videoBackground.start()
//    }
}