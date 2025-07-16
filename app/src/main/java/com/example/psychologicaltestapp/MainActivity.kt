package com.example.psychologicaltestapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.example.psychologicaltestapp.databinding.ActivityMainBinding
import com.example.psychologicaltestapp.utils.DialogHelper
import com.example.psychologicaltestapp.utils.PremiumManager
import kotlinx.coroutines.*
import java.util.*

class MainActivity : BaseActivity() {
    companion object {
        const val SETTINGS_REQUEST_CODE = 1001
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TopBar gesture listener
        val topBar = findViewById<View>(R.id.topBar)
        setupTopBar()

        auth = FirebaseAuth.getInstance()


        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (distanceY < -30) { // gesto hacia abajo
                    topBar.visibility = View.VISIBLE
                }
                return true
            }
        })

        binding.scrollView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        setupButtons()
        applyPremiumButtonAnimation()
        updateUI()
        setupAdMob()
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
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, SETTINGS_REQUEST_CODE)
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

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = prefs.getString("app_language", "es") ?: "es"
        val context = LanguageHelper.setLocale(newBase, language)
        super.attachBaseContext(context)
    }

    object LanguageHelper {
        fun setLocale(context: Context, language: String): Context {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            recreate()
        }
    }
}
