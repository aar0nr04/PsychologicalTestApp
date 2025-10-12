package com.example.psychologicaltestapp.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object Dialogs {
    fun simpleMessage(context: Context, title: String, message: String) =
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()
}
