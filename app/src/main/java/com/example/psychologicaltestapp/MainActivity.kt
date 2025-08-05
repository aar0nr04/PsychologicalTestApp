package com.example.psychologicaltestapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.psychologicaltestapp.databinding.ActivityMainRedesignedBinding
import com.example.psychologicaltestapp.utils.DialogHelper
import com.example.psychologicaltestapp.utils.PremiumManager
import com.google.firebase.auth.FirebaseAuth
import java.util.*

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
        updateUI()
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener {
            if (auth.currentUser != null) {
                auth.signOut()
                updateUI()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
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
            startActivity(Intent(this, TestListActivity::class.java))
        }

        binding.btnDirectory.setOnClickListener {
            startActivity(Intent(this, PsychologistDirectoryActivity::class.java))
        }

        binding.btnPremium.setOnClickListener {
            if (PremiumManager.isUserPremium(this)) {
                Toast.makeText(this, "¡Ya eres Premium!", Toast.LENGTH_SHORT).show()
            } else {
                DialogHelper.mostrarDialogoPremium(this)
            }
        }
    }

    private fun applyPremiumButtonAnimation() {
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.btnPremium.startAnimation(pulse)
    }

    private fun updateUI() {
        if (auth.currentUser != null) {
            binding.btnLogin.text = "Cerrar sesión"
            binding.btnRegister.isEnabled = false
        } else {
            binding.btnLogin.text = "Iniciar sesión"
            binding.btnRegister.isEnabled = true
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
