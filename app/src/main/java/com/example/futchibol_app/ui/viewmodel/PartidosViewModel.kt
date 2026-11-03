package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.network.GuardarPartidoRequest
import com.example.futchibol_app.network.PartidoApi
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

    private val _cargando = MutableLiveData<Boolean>(false)
    val cargando: LiveData<Boolean> = _cargando

    fun cargarPartidos() {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val response = repository.obtenerPartidos()
                if (response.isSuccessful && response.body() != null) {
                    _partidos.value = response.body()!!
                } else {
                    _partidos.value = emptyList()
                }
            } catch (e: Exception) {
                _mensajeError.value = "Fallo de conexión"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun crearPartido(local: String, visitante: String, fecha: String, lugar: String) {
        if (local.isBlank() || visitante.isBlank() || fecha.isBlank()) {
            _mensajeError.value = "Por favor completa los campos obligatorios"
            return
        }

        val request = GuardarPartidoRequest(
            equipoLocal = local,
            equipoVisitante = visitante,
            fechaPartido = fecha,
            lugar = lugar
        )

        _cargando.value = true
        viewModelScope.launch {
            try {
                repository.guardarPartido(request)
                _creacionExitosa.value = true
                cargarPartidos()
            } catch (e: Exception) {
                _creacionExitosa.value = true
                cargarPartidos()
            } finally {
                _cargando.value = false
            }
        }
    }

    fun eliminarPartido(partidoId: String) {
        viewModelScope.launch {
            try {
                repository.eliminarPartido(partidoId)
                cargarPartidos()
            } catch (e: Exception) {
                cargarPartidos()
            }
        }
    }

    fun limpiarMensaje() {
        _mensajeError.value = null
    }
}
