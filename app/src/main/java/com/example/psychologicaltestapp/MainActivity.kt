package com.example.psychologicaltestapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.psychologicaltestapp.databinding.ActivityMainRedesignedBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import com.example.psychologicaltestapp.ui.auth.LoginActivity
import com.example.psychologicaltestapp.ui.tests.TestsCatalogActivity
import com.example.psychologicaltestapp.ui.premium.PremiumPlansActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainRedesignedBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainRedesignedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupButtons()
        applyPremiumButtonAnimation()
    }

    override fun onStart() {
        super.onStart()
        ensureAuthenticated()
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener {
            if (auth.currentUser != null) {
                auth.signOut()
                navigateToLogin(clearTask = true)
            } else {
                navigateToLogin(clearTask = false)
            }
        }

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnConfig.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnTest.setOnClickListener {
            startActivity(Intent(this, TestsCatalogActivity::class.java))
        }

        binding.btnDirectory.setOnClickListener {
            startActivity(Intent(this, PsychologistDirectoryActivity::class.java))
        }

        binding.btnPremium.isVisible = true
        binding.btnPremium.setOnClickListener {
            startActivity(Intent(this, PremiumPlansActivity::class.java))
        }
    }

    private fun applyPremiumButtonAnimation() {
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.btnPremium.startAnimation(pulse)
    }

    private fun updateUI() {
        if (auth.currentUser != null) {
            binding.btnLogin.text = "\uD83D\uDD13 Cerrar sesión"
            binding.btnRegister.isEnabled = false
        } else {
            binding.btnLogin.text = "\uD83D\uDD10 Iniciar sesión"
            binding.btnRegister.isEnabled = true
        }
    }

    private fun ensureAuthenticated() {
        if (auth.currentUser == null) {
            navigateToLogin(clearTask = true)
        } else {
            updateUI()
        }
    }

    private fun navigateToLogin(clearTask: Boolean) {
        val intent = Intent(this, LoginActivity::class.java)
        if (clearTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        if (clearTask) {
            finish()
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
}
