package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.Constants
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.network.GuardarJugadorRequest
import kotlinx.coroutines.launch

class NuevoJugadorViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _exitoGuardar = MutableLiveData<Boolean>()
    val exitoGuardar: LiveData<Boolean> = _exitoGuardar

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData<Boolean>(false)
    val cargando: LiveData<Boolean> = _cargando

    fun guardarJugador(nombre: String, dorsalStr: String, posicion: String, equipoId: String = Constants.DEFAULT_EQUIPO_ID) {
        if (nombre.isBlank() || dorsalStr.isBlank()) {
            _mensajeError.value = "Completa los campos obligatorios"
            return
        }

        val dorsal = dorsalStr.toIntOrNull()
        if (dorsal == null || dorsal <= 0) {
            _mensajeError.value = "Ingresa un dorsal válido"
            return
        }

        val request = GuardarJugadorRequest(
            nombre = nombre,
            dorsal = dorsal,
            posicion = posicion,
            equipo = equipoId
        )

        _cargando.value = true
        viewModelScope.launch {
            try {
                val response = repository.guardarJugador(request)
                if (response.isSuccessful) {
                    _exitoGuardar.value = true
                } else {
                    _mensajeError.value = "Error al registrar jugador: ${response.code()}"
                }
            } catch (e: Exception) {
                _mensajeError.value = "Fallo de conexión"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun limpiarMensaje() {
        _mensajeError.value = null
    }
}
