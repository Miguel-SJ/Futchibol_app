package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.data.Resultado
import com.example.futchibol_app.network.*
import kotlinx.coroutines.launch

class PartidosViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _partidos = MutableLiveData<List<PartidoApi>>(emptyList())
    val partidos: LiveData<List<PartidoApi>> = _partidos

    private val _creacionExitosa = MutableLiveData<Boolean>()
    val creacionExitosa: LiveData<Boolean> = _creacionExitosa

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    var equipos: List<EquipoApi> = emptyList()
        private set

    var equipoActual: EquipoApi? = null
        private set

    fun cargarPartidos() {
        _cargando.value = true
        viewModelScope.launch {
            equipoActual = repository.equipoActual().getOrNull()
            equipos = repository.obtenerEquipos().getOrNull().orEmpty()
            when (val r = repository.obtenerPartidos()) {
                is Resultado.Ok -> _partidos.value = r.datos
                is Resultado.Fallo -> _mensajeError.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    fun crearPartido(local: EquipoApi?, visitante: EquipoApi?, fecha: String, lugar: String) {
        if (local == null || visitante == null || fecha.isBlank()) {
            _mensajeError.value = "Por favor completa los campos obligatorios"
            return
        }
        val actual = equipoActual
        if (actual != null && actual.id != local.id && actual.id != visitante.id) {
            _mensajeError.value = "Uno de los dos equipos debe ser ${actual.nombre}, tu equipo actual"
            return
        }
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.crearPartido(local.id, visitante.id, fecha, lugar)) {
                is Resultado.Ok -> {
                    _creacionExitosa.value = true
                    cargarPartidos()
                }
                is Resultado.Fallo -> {
                    _mensajeError.value = r.mensaje
                    _cargando.value = false
                }
            }
        }
    }

    fun eliminarPartido(partidoId: String) {
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.eliminarPartido(partidoId)) {
                is Resultado.Ok -> cargarPartidos()
                is Resultado.Fallo -> {
                    _mensajeError.value = r.mensaje
                    _cargando.value = false
                }
            }
        }
    }

    fun limpiarMensaje() {
        _mensajeError.value = null
    }
}
