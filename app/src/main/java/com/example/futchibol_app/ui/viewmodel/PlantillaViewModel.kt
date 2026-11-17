package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.data.Resultado
import com.example.futchibol_app.network.*
import kotlinx.coroutines.launch

class PlantillaViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _listaCompleta = MutableLiveData<List<JugadorApi>>(emptyList())
    val listaCompleta: LiveData<List<JugadorApi>> = _listaCompleta

    private val _listaFiltrada = MutableLiveData<List<JugadorApi>>(emptyList())
    val listaFiltrada: LiveData<List<JugadorApi>> = _listaFiltrada

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    var nombreEquipo: String = ""
        private set
    var filtroActual = "TODOS"
    var busquedaActual = ""

    fun cargarJugadores() {
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.obtenerPlantilla()) {
                is Resultado.Ok -> {
                    nombreEquipo = r.datos.equipo.nombre
                    _listaCompleta.value = r.datos.jugadores
                    aplicarFiltros()
                }
                is Resultado.Fallo -> _mensajeError.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    fun cambiarFiltro(nuevoFiltro: String) {
        filtroActual = nuevoFiltro
        aplicarFiltros()
    }

    fun buscar(query: String) {
        busquedaActual = query.trim().lowercase()
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val base = _listaCompleta.value ?: emptyList()
        var resultado = when (filtroActual) {
            "HABILITADOS" -> base.filter { it.activo }
            "INHABILITADOS" -> base.filter { !it.activo }
            else -> base
        }
        if (busquedaActual.isNotEmpty()) {
            resultado = resultado.filter {
                it.nombre.lowercase().contains(busquedaActual) || it.dorsal.toString() == busquedaActual
            }
        }
        _listaFiltrada.value = resultado
    }
}
