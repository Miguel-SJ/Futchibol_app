package com.example.futchibol_app.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Fechas {
    fun hoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
