package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.network.JugadorApi
import kotlinx.coroutines.launch

class InhabilitacionesViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _inhabilitados = MutableLiveData<List<JugadorApi>>(emptyList())
    val inhabilitados: LiveData<List<JugadorApi>> = _inhabilitados

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData<Boolean>(false)
    val cargando: LiveData<Boolean> = _cargando

    fun cargarInhabilitados() {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val respInhabilitados = repository.obtenerJugadoresInhabilitados()
                val respTodos = repository.obtenerJugadores()

                val mapaInhabilitados = mutableMapOf<String, JugadorApi>()

                if (respInhabilitados.isSuccessful && respInhabilitados.body() != null) {
                    respInhabilitados.body()!!.forEach { mapaInhabilitados[it.id] = it }
                }

                if (respTodos.isSuccessful && respTodos.body() != null) {
                    respTodos.body()!!.filter { !it.activo }.forEach { mapaInhabilitados[it.id] = it }
                }

                _inhabilitados.value = mapaInhabilitados.values.toList()
            } catch (e: Exception) {
                _mensajeError.value = "Error al obtener inhabilitados"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resolverInhabilitacion(jugadorId: String) {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val response = repository.resolverInhabilitacion(jugadorId)
                if (response.isSuccessful) {
                    cargarInhabilitados()
                } else {
                    cargarInhabilitados()
                }
            } catch (e: Exception) {
                cargarInhabilitados()
            } finally {
                _cargando.value = false
            }
        }
    }

    fun limpiarMensaje() {
        _mensajeError.value = null
    }
}
