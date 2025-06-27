package com.example.psychologicaltestapp.utils

import android.content.Context

object PremiumManager {

    fun isUserPremium(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_premium", false)
    }

    fun setUserPremium(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_premium", value).apply()
    }
}
