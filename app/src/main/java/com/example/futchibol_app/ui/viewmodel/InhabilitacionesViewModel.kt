package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.data.Resultado
import com.example.futchibol_app.network.*
import kotlinx.coroutines.launch

class InhabilitacionesViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _inhabilitados = MutableLiveData<List<JugadorApi>>(emptyList())
    val inhabilitados: LiveData<List<JugadorApi>> = _inhabilitados

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    fun cargarInhabilitados() {
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.obtenerPlantilla()) {
                is Resultado.Ok -> _inhabilitados.value = r.datos.jugadores.filter { !it.activo }
                is Resultado.Fallo -> _mensajeError.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    fun limpiarMensaje() {
        _mensajeError.value = null
    }
}
