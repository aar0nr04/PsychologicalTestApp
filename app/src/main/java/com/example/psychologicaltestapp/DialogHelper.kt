package com.example.psychologicaltestapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast

object DialogHelper {

    fun mostrarDialogoPremium(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Hazte Premium")
            .setMessage("Desbloquea beneficios:\n✔ Tips AI ilimitados\n✔ Tests exclusivos (próximamente)\n✔ Mejores resultados\n\n¿Quieres ser Premium?")
            .setPositiveButton("Sí, quiero") { _, _ ->
                PremiumManager.setUserPremium(activity, true)
                Toast.makeText(activity, "¡Ahora eres Premium!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
