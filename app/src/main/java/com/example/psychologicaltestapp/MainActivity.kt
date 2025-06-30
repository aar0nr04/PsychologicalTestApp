package com.example.psychologicaltestapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.example.psychologicaltestapp.databinding.ActivityMainBinding
import android.graphics.Color
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.example.psychologicaltestapp.utils.DialogHelper
import com.example.psychologicaltestapp.utils.PremiumManager
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null
    private lateinit var flagEs: ImageView
    private lateinit var flagEn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLocale()
        applySavedLanguage()
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = prefs.getString("app_language", "es") ?: "es"


        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        setupAdMob()
        setupButtons()
        applyPremiumButtonAnimation()
        updateUI()
    }
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", languageCode).apply()
    }
    private fun setupAdMob() {
        MobileAds.initialize(this) {}

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        com.google.android.gms.ads.interstitial.InterstitialAd.load(
            this,
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
    private fun setupButtons() {
        binding.loginButton.setOnClickListener {
            if (auth.currentUser != null) {
                auth.signOut()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        binding.testsButton.setOnClickListener {
            if (interstitialAd != null) {
                interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        abrirTestListActivity()
                        cargarOtroInterstitial()
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
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }


        binding.buttonPremium.setOnClickListener {
            if (PremiumManager.isUserPremium(this)) {
                Toast.makeText(this, "¡Ya eres Premium!", Toast.LENGTH_SHORT).show()
            } else {
                DialogHelper.mostrarDialogoPremium(this)
            }
        }
    }
    private fun cargarOtroInterstitial() {
        val adRequest = AdRequest.Builder().build()
        com.google.android.gms.ads.interstitial.InterstitialAd.load(
            this,
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
    private fun applyPremiumButtonAnimation() {
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.buttonPremium.startAnimation(pulse)
    }
    private fun abrirTestListActivity() {
        startActivity(Intent(this, TestListActivity::class.java))
    }
    private fun updateUI() {
        if (this::auth.isInitialized && auth.currentUser != null) {
            binding.loginButton.text = "Cerrar sesión"
            binding.testsButton.isEnabled = true
            binding.psychologistDirectoryButton.isEnabled = true
            binding.registerButton.isEnabled = false
        } else {
            binding.loginButton.text = "Iniciar sesión"
            binding.testsButton.isEnabled = true
            binding.psychologistDirectoryButton.isEnabled = true
            binding.registerButton.isEnabled = true
        }
    }
    private fun applySavedLanguage() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("app_language", Locale.getDefault().language) ?: "es"

        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    private fun loadLocale() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = prefs.getString("app_language", Locale.getDefault().language) ?: "es"
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
