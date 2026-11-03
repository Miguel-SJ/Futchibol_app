package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.network.JugadorApi
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

    private val _cargando = MutableLiveData<Boolean>(false)
    val cargando: LiveData<Boolean> = _cargando

    var filtroActual = "TODOS"
    var busquedaActual = ""

    fun cargarJugadores() {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val respTodos = repository.obtenerJugadores()
                val respInhabilitados = repository.obtenerJugadoresInhabilitados()

                if (respTodos.isSuccessful && respTodos.body() != null) {
                    val todos = respTodos.body()!!
                    val inhabilitadosIds = if (respInhabilitados.isSuccessful && respInhabilitados.body() != null) {
                        respInhabilitados.body()!!.map { it.id }.toSet()
                    } else emptySet()

                    val listaActualizada = todos.map { jug ->
                        if (inhabilitadosIds.contains(jug.id)) jug.copy(activo = false) else jug
                    }

                    _listaCompleta.value = listaActualizada
                    aplicarFiltros()
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
