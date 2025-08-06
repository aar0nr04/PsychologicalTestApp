package com.example.psychologicaltestapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import java.util.*

open class BaseActivity : AppCompatActivity() {

    lateinit var globalHeader: View
    lateinit var rootLayout: ViewGroup

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

    override fun setContentView(layoutResID: Int) {
        val base = layoutInflater.inflate(R.layout.activity_base, null)
        val contentFrame = base.findViewById<FrameLayout>(R.id.content_frame)
        layoutInflater.inflate(layoutResID, contentFrame, true)
        super.setContentView(base)

        // 👇 Esta línea es necesaria
        rootLayout = base.findViewById(R.id.rootLayout)  // O el ID correcto

        globalHeader = findViewById(R.id.view_global_header)

        setupGestureListener()
        restoreHeaderVisibility()
    }

    private fun setupGestureListener() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            @Suppress("NOTHING_TO_OVERRIDE", "ACCIDENTAL_OVERRIDE")
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false
                val diffY = e2.y - e1.y
                if (Math.abs(diffY) > 100 && Math.abs(velocityY) > 100) {
                    if (diffY > 0) {
                        showHeader()
                    } else {
                        hideHeader()
                    }
                    return true
                }
                return false
            }
        })

        rootLayout.setOnTouchListener { it, event ->
            gestureDetector.onTouchEvent(event)

            if (event.action == MotionEvent.ACTION_UP) {
                it.performClick()
            }

            true
        }
    }

    private fun showHeader() {
        globalHeader.visibility = View.VISIBLE
        saveHeaderVisibility(true)
    }

    private fun hideHeader() {
        globalHeader.visibility = View.GONE
        saveHeaderVisibility(false)
    }

    private fun saveHeaderVisibility(isVisible: Boolean) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("header_visible", isVisible).apply()
    }

    private fun restoreHeaderVisibility() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val visible = prefs.getBoolean("header_visible", false)
        globalHeader.visibility = if (visible) View.VISIBLE else View.GONE
    }
}
