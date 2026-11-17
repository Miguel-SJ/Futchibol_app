package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.data.Resultado
import com.example.futchibol_app.network.*
import kotlinx.coroutines.launch

class IncidenciaViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _jugadores = MutableLiveData<List<JugadorApi>>(emptyList())
    val jugadores: LiveData<List<JugadorApi>> = _jugadores

    private val _nombreEquipo = MutableLiveData<String>()
    val nombreEquipo: LiveData<String> = _nombreEquipo

    private val _exitoRegistro = MutableLiveData<Boolean>()
    val exitoRegistro: LiveData<Boolean> = _exitoRegistro

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    fun cargarJugadores() {
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.obtenerPlantilla()) {
                is Resultado.Ok -> {
                    _nombreEquipo.value = r.datos.equipo.nombre
                    _jugadores.value = r.datos.jugadores.filter { it.activo }
                }
                is Resultado.Fallo -> _mensajeError.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    fun registrarIncidencia(
        jugadorId: String,
        motivo: String,
        descripcion: String,
        fechaInicio: String,
        fechaFin: String
    ) {
        if (jugadorId.isEmpty()) {
            _mensajeError.value = "Seleccione un jugador válido"
            return
        }
        if (fechaInicio.isNotEmpty() && fechaFin.isNotEmpty() && fechaFin < fechaInicio) {
            _mensajeError.value = "La fecha fin no puede ser anterior a la de inicio"
            return
        }
        val request = GuardarInhabilitacionRequest(
            jugador = jugadorId,
            motivo = motivo,
            descripcion = descripcion.ifBlank { null },
            fechaFin = fechaFin.takeIf { it.isNotEmpty() }?.let { "${it}T00:00:00.000Z" }
        )
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.guardarInhabilitacion(request)) {
                is Resultado.Ok -> _exitoRegistro.value = true
                is Resultado.Fallo -> _mensajeError.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    fun limpiarMensaje() {
        _mensajeError.value = null
    }
}
