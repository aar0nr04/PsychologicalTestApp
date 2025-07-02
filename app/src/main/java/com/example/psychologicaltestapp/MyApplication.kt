package com.example.psychologicaltestapp

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.google.firebase.FirebaseApp
import java.util.*

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        applyAppLocale()
    }

    private fun applyAppLocale() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = prefs.getString("app_language", "es") ?: "es"
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
