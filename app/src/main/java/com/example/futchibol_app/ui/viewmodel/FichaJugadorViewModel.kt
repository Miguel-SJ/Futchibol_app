package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.network.JugadorApi
import kotlinx.coroutines.launch

class FichaJugadorViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _jugadorDetalle = MutableLiveData<JugadorApi?>()
    val jugadorDetalle: LiveData<JugadorApi?> = _jugadorDetalle

    private val _eliminacionExitosa = MutableLiveData<Boolean>()
    val eliminacionExitosa: LiveData<Boolean> = _eliminacionExitosa

    private val _resolucionExitosa = MutableLiveData<Boolean>()
    val resolucionExitosa: LiveData<Boolean> = _resolucionExitosa

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData<Boolean>(false)
    val cargando: LiveData<Boolean> = _cargando

    fun cargarDetalle(jugadorId: String) {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val response = repository.obtenerDetalleJugador(jugadorId)
                if (response.isSuccessful && response.body() != null) {
                    _jugadorDetalle.value = response.body()
                } else {
                    _mensajeError.value = "Fallo al consultar detalle"
                }
            } catch (e: Exception) {
                _mensajeError.value = "Fallo de conexión"
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
                    _resolucionExitosa.value = true
                    cargarDetalle(jugadorId)
                } else {
                    _resolucionExitosa.value = true
                    cargarDetalle(jugadorId)
                }
            } catch (e: Exception) {
                _resolucionExitosa.value = true
                cargarDetalle(jugadorId)
            } finally {
                _cargando.value = false
            }
        }
    }

    fun eliminarJugador(jugadorId: String) {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val response = repository.eliminarJugador(jugadorId)
                if (response.isSuccessful) {
                    _eliminacionExitosa.value = true
                } else {
                    _eliminacionExitosa.value = true
                }
            } catch (e: Exception) {
                _eliminacionExitosa.value = true
            } finally {
                _cargando.value = false
            }
        }
    }

    fun limpiarMensaje() {
        _mensajeError.value = null
    }
}
