package com.example.psychologicaltestapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.*

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    protected fun setupTopBar() {
        val topBar = findViewById<View>(R.id.topBar) ?: return
        var lastY = 0f

        findViewById<View>(android.R.id.content)?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> lastY = event.y
                MotionEvent.ACTION_UP -> {
                    val deltaY = event.y - lastY
                    if (deltaY > 100) {
                        topBar.visibility = View.VISIBLE
                    } else if (deltaY < -100) {
                        topBar.visibility = View.GONE
                    }
                }
            }
            false
        }
    }
}
