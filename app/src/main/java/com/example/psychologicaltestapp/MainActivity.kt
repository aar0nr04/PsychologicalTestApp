package com.example.psychologicaltestapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.example.psychologicaltestapp.databinding.ActivityMainBinding
import android.graphics.Color
import android.widget.ImageView
import androidx.appcompat.widget.SwitchCompat

import java.util.*
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null

    // Declare the views as member properties

    private lateinit var flagEs: ImageView
    private lateinit var flagEn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {

        // Cargar idioma guardado antes de setContentView
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = prefs.getString("app_language", "es") ?: "es"
        setLocale(language)

        super.onCreate(savedInstanceState)
        // It's generally recommended to initialize binding before accessing views
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // Use binding.root
        val languageSwitch = findViewById<SwitchCompat>(R.id.languageSwitch)
        // Initialize the views using findViewById or ViewBinding if available
        // Or use binding.languageSwitch if you have it in your layout and ViewBinding is setup correctly
        flagEs = findViewById(R.id.flag_es) // Or use binding.flagEs
        flagEn = findViewById(R.id.flag_en) // Or use binding.flagEn

        // Inicializar switch y colores de bandera según idioma guardado
        languageSwitch.isChecked = (language == "en")
        updateFlagColors(language)

        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            val newLang = if (isChecked) "en" else "es"
            setLocale(newLang)
            updateFlagColors(newLang)
            recreate() // Recarga la actividad para aplicar idioma
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance() // Make sure auth is initialized

        setupAdMob()
        setupButtons()
        updateUI() // Call updateUI after auth is initialized and buttons are set up
    }
    private fun updateFlagColors(languageCode: String) {
        if (languageCode == "es") {
            flagEs.clearColorFilter()
            flagEn.setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.SRC_IN)
        } else {
            flagEn.clearColorFilter()
            flagEs.setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Guardar idioma en preferencias
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", languageCode).apply()
    }
    private fun setupAdMob() {
        MobileAds.initialize(this) {}

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        // Cargar Interstitial Ad
        com.google.android.gms.ads.interstitial.InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712", // ID de prueba de Google, cambia en producción
            adRequest,
            object : com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: com.google.android.gms.ads.interstitial.InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                    interstitialAd = null
                }
            }
        )
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
            if (interstitialAd != null) {
                interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        abrirTestListActivity()

                        // Recargar otro Interstitial Ad después de mostrarlo
                        val adRequest = AdRequest.Builder().build()
                        com.google.android.gms.ads.interstitial.InterstitialAd.load(
                            this@MainActivity,
                            "ca-app-pub-3940256099942544/1033173712",
                            adRequest,
                            object : com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                                override fun onAdLoaded(ad: com.google.android.gms.ads.interstitial.InterstitialAd) {
                                    interstitialAd = ad
                                }

                                override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                                    interstitialAd = null
                                }
                            }
                        )
                    }
                }
                interstitialAd?.show(this)
            } else {
                abrirTestListActivity()
            }
        }


        binding.psychologistDirectoryButton.setOnClickListener {
            startActivity(Intent(this, PsychologistDirectoryActivity::class.java))
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
    private fun abrirTestListActivity() {
        startActivity(Intent(this, TestListActivity::class.java))
    }

    private fun updateUI() {
        if (this::auth.isInitialized && auth.currentUser != null) { // Check if auth is initialized
            binding.loginButton.text = "Cerrar sesión"
            binding.testsButton.isEnabled = true
            binding.psychologistDirectoryButton.isEnabled = true
            binding.registerButton.isEnabled = false
        } else {
            binding.loginButton.text = "Iniciar sesión"
            binding.testsButton.isEnabled = true // Or false depending on your logic when not logged in
            binding.psychologistDirectoryButton.isEnabled = true // Or false
            binding.registerButton.isEnabled = true
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
