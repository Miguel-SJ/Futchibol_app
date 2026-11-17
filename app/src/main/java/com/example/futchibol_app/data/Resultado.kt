package com.example.futchibol_app.data

sealed class Resultado<out T> {
    data class Ok<out T>(val datos: T) : Resultado<T>()
    data class Fallo(val mensaje: String, val codigo: Int? = null) : Resultado<Nothing>()

    fun getOrNull(): T? = (this as? Ok<T>)?.datos
}
