package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.data.Resultado
import com.example.futchibol_app.network.*
import kotlinx.coroutines.launch

class NuevoJugadorViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _nombreEquipo = MutableLiveData<String>()
    val nombreEquipo: LiveData<String> = _nombreEquipo

    private val _exitoGuardar = MutableLiveData<Boolean>()
    val exitoGuardar: LiveData<Boolean> = _exitoGuardar

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    fun cargarEquipo() {
        viewModelScope.launch {
            when (val r = repository.equipoActual()) {
                is Resultado.Ok -> _nombreEquipo.value = r.datos.nombre
                is Resultado.Fallo -> _mensajeError.value = r.mensaje
            }
        }
    }

    fun guardarJugador(nombre: String, dorsalStr: String, posicion: String) {
        if (nombre.isBlank() || dorsalStr.isBlank()) {
            _mensajeError.value = "Completa los campos obligatorios"
            return
        }
        val dorsal = dorsalStr.toIntOrNull()
        if (dorsal == null || dorsal <= 0) {
            _mensajeError.value = "Ingresa un dorsal válido"
            return
        }
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.guardarJugador(nombre.trim(), dorsal, posicion)) {
                is Resultado.Ok -> _exitoGuardar.value = true
                is Resultado.Fallo -> _mensajeError.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    fun limpiarMensaje() {
        _mensajeError.value = null
    }
}
