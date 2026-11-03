package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.network.GuardarAlineacionRequest
import com.example.futchibol_app.network.JugadorApi
import kotlinx.coroutines.launch

class AlineacionViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _jugadoresDisponibles = MutableLiveData<List<JugadorApi>>(emptyList())
    val jugadoresDisponibles: LiveData<List<JugadorApi>> = _jugadoresDisponibles

    private val _jugadoresSeleccionados = MutableLiveData<Set<JugadorApi>>(emptySet())
    val jugadoresSeleccionados: LiveData<Set<JugadorApi>> = _jugadoresSeleccionados

    private val _guardadoExitoso = MutableLiveData<Boolean>()
    val guardadoExitoso: LiveData<Boolean> = _guardadoExitoso

    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> = _mensaje

    private val _cargando = MutableLiveData<Boolean>(false)
    val cargando: LiveData<Boolean> = _cargando

    fun cargarJugadoresHabilitados(partidoId: String?) {
        val idFinal = if (partidoId.isNullOrEmpty()) "default_partido" else partidoId
        _cargando.value = true
        viewModelScope.launch {
            try {
                val response = repository.obtenerJugadores()
                if (response.isSuccessful && response.body() != null) {
                    val disponibles = response.body()!!.filter { it.activo }
                    _jugadoresDisponibles.value = disponibles

                    val guardadosIds = repository.obtenerAlineacionGuardadaIds(idFinal)
                    if (guardadosIds.isNotEmpty()) {
                        val preseleccionados = disponibles.filter { guardadosIds.contains(it.id) }.toSet()
                        _jugadoresSeleccionados.value = preseleccionados
                    }
                } else {
                    _mensaje.value = "Error al cargar jugadores disponibles"
                }
            } catch (e: Exception) {
                _mensaje.value = "Fallo de conexión al cargar jugadores"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun toggleSeleccion(jugador: JugadorApi) {
        val actuales = _jugadoresSeleccionados.value?.toMutableSet() ?: mutableSetOf()
        val yaExiste = actuales.any { it.id == jugador.id }

        if (yaExiste) {
            actuales.removeAll { it.id == jugador.id }
        } else {
            if (actuales.size >= 11) {
                _mensaje.value = "Máximo 11 jugadores permitidos"
                return
            }
            actuales.add(jugador)
        }
        _jugadoresSeleccionados.value = actuales
    }

    fun guardarAlineacion(partidoId: String?) {
        val seleccionados = _jugadoresSeleccionados.value ?: emptySet()
        if (seleccionados.size < 7) {
            _mensaje.value = "Se requieren al menos 7 jugadores para armar alineación"
            return
        }

        val ids = seleccionados.map { it.id }
        val idFinal = if (partidoId.isNullOrEmpty()) "default_partido" else partidoId

        _cargando.value = true
        viewModelScope.launch {
            try {
                val request = GuardarAlineacionRequest(
                    tipoEquipo = "titular",
                    jugadoresIds = ids
                )
                repository.guardarAlineacion(idFinal, request)
                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _guardadoExitoso.value = true
            } finally {
                _cargando.value = false
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
