package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.data.Resultado
import com.example.futchibol_app.network.*
import kotlinx.coroutines.launch

class FichaJugadorViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _jugadorDetalle = MutableLiveData<JugadorApi?>()
    val jugadorDetalle: LiveData<JugadorApi?> = _jugadorDetalle

    private val _historial = MutableLiveData<List<InhabilitacionApi>>(emptyList())
    val historial: LiveData<List<InhabilitacionApi>> = _historial

    private val _eliminacionExitosa = MutableLiveData<Boolean>()
    val eliminacionExitosa: LiveData<Boolean> = _eliminacionExitosa

    private val _resolucionExitosa = MutableLiveData<Boolean>()
    val resolucionExitosa: LiveData<Boolean> = _resolucionExitosa

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    fun cargarDetalle(jugadorId: String) {
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.obtenerFicha(jugadorId)) {
                is Resultado.Ok -> {
                    _jugadorDetalle.value = r.datos.jugador
                    _historial.value = r.datos.historial
                }
                is Resultado.Fallo -> _mensajeError.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    fun resolverInhabilitacion(jugadorId: String) {
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.resolverInhabilitaciones(jugadorId)) {
                is Resultado.Ok -> {
                    _resolucionExitosa.value = true
                    cargarDetalle(jugadorId)
                }
                is Resultado.Fallo -> {
                    _mensajeError.value = r.mensaje
                    _cargando.value = false
                }
            }
        }
    }

    fun eliminarJugador(jugadorId: String) {
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.eliminarJugador(jugadorId)) {
                is Resultado.Ok -> _eliminacionExitosa.value = true
                is Resultado.Fallo -> _mensajeError.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    fun limpiarMensaje() {
        _mensajeError.value = null
    }
}
