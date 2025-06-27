package com.example.psychologicaltestapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var languageSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        languageSpinner = findViewById(R.id.languageSpinner)

        val languages =
            arrayOf("Español", "English", "Français", "Português", "Italiano", "Deutsch", "中文")
        val languageCodes = arrayOf("es", "en", "fr", "pt", "it", "de", "zh")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val currentLang = prefs.getString("app_language", Locale.getDefault().language) ?: "es"
        val selectedIndex = languageCodes.indexOf(currentLang).takeIf { it >= 0 } ?: 0
        languageSpinner.setSelection(selectedIndex)

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newLang = languageCodes[position]
                if (newLang != currentLang) {
                    setLocale(newLang)
                    recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada
            }
        }
    }
    fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", languageCode).apply()
    }
}
