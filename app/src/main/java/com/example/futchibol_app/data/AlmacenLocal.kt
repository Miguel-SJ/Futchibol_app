package com.example.futchibol_app.data

import android.content.Context
import com.example.futchibol_app.FutbolApp

class AlmacenLocal(context: Context = FutbolApp.context) {

    private val prefs = context.getSharedPreferences("futbol_app_prefs_v3", Context.MODE_PRIVATE)

    var nombreTorneo: String
        get() = prefs.getString(KEY_TORNEO, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TORNEO, value).apply()

    var equipoId: String?
        get() = prefs.getString(KEY_EQUIPO_ID, null)
        set(value) = prefs.edit().putString(KEY_EQUIPO_ID, value).apply()

    var equipoNombre: String?
        get() = prefs.getString(KEY_EQUIPO_NOMBRE, null)
        set(value) = prefs.edit().putString(KEY_EQUIPO_NOMBRE, value).apply()

    private companion object {
        const val KEY_TORNEO = "torneo"
        const val KEY_EQUIPO_ID = "equipo_id"
        const val KEY_EQUIPO_NOMBRE = "equipo_nombre"
    }
}
