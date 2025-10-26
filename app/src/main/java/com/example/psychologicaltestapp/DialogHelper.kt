package com.example.psychologicaltestapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import com.example.psychologicaltestapp.BuildConfig
import com.example.psychologicaltestapp.R

object DialogHelper {

    fun mostrarDialogoPremium(activity: Activity) {
        if (!BuildConfig.PREMIUM_ENABLED) {
            AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.premium_unavailable_title))
                .setMessage(activity.getString(R.string.premium_unavailable_message))
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.premium_paywall_title))
            .setMessage(activity.getString(R.string.premium_paywall_message))
            .setPositiveButton(activity.getString(R.string.premium_paywall_positive)) { _, _ ->
                PremiumManager.setUserPremium(activity, true)
                Toast.makeText(activity, activity.getString(R.string.premium_paywall_success), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
