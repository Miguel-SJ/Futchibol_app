package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.network.GuardarInhabilitacionRequest
import com.example.futchibol_app.network.JugadorApi
import kotlinx.coroutines.launch

class IncidenciaViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _jugadores = MutableLiveData<List<JugadorApi>>(emptyList())
    val jugadores: LiveData<List<JugadorApi>> = _jugadores

    private val _exitoRegistro = MutableLiveData<Boolean>()
    val exitoRegistro: LiveData<Boolean> = _exitoRegistro

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData<Boolean>(false)
    val cargando: LiveData<Boolean> = _cargando

    fun cargarJugadores() {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val response = repository.obtenerJugadores()
                if (response.isSuccessful && response.body() != null) {
                    _jugadores.value = response.body()!!
                } else {
                    _mensajeError.value = "Error al obtener jugadores"
                }
            } catch (e: Exception) {
                _mensajeError.value = "Fallo de conexión"
            } finally {
                _cargando.value = false
            }
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
        if (fechaInicio.isEmpty()) {
            _mensajeError.value = "Seleccione una fecha de inicio"
            return
        }

        val request = GuardarInhabilitacionRequest(
            jugador = jugadorId,
            motivo = motivo.ifEmpty { "tarjeta_roja" },
            descripcion = descripcion.ifEmpty { "Inhabilitación registrada desde la app" },
            fechaInicio = "${fechaInicio}T00:00:00.000Z",
            fechaFin = if (fechaFin.isNotEmpty()) "${fechaFin}T00:00:00.000Z" else "${fechaInicio}T00:00:00.000Z"
        )

        _cargando.value = true
        viewModelScope.launch {
            try {
                val response = repository.guardarInhabilitacion(request)
                if (response.isSuccessful) {
                    _exitoRegistro.value = true
                } else {
                    _mensajeError.value = "Error al guardar la incidencia"
                }
            } catch (e: Exception) {
                _mensajeError.value = "Fallo de conexión al guardar"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun limpiarMensaje() {
        _mensajeError.value = null
    }
}
