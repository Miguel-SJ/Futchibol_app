package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.data.Resultado
import com.example.futchibol_app.network.*
import kotlinx.coroutines.launch

class AlineacionViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _jugadoresDisponibles = MutableLiveData<List<JugadorApi>>(emptyList())
    val jugadoresDisponibles: LiveData<List<JugadorApi>> = _jugadoresDisponibles

    private val _jugadoresSeleccionados = MutableLiveData<Set<JugadorApi>>(emptySet())
    val jugadoresSeleccionados: LiveData<Set<JugadorApi>> = _jugadoresSeleccionados

    private val _titulo = MutableLiveData<String>()
    val titulo: LiveData<String> = _titulo

    private val _guardadoExitoso = MutableLiveData<Boolean>()
    val guardadoExitoso: LiveData<Boolean> = _guardadoExitoso

    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> = _mensaje

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private var tipoEquipo: String? = null

    fun cargarJugadoresHabilitados(partidoId: String?, conservarSeleccion: Boolean = false) {
        if (partidoId == null) {
            _mensaje.value = "No se encontró la jornada"
            return
        }
        _cargando.value = true
        viewModelScope.launch {
            val partido = when (val rp = repository.obtenerPartido(partidoId)) {
                is Resultado.Ok -> rp.datos
                is Resultado.Fallo -> {
                    _mensaje.value = rp.mensaje
                    _cargando.value = false
                    return@launch
                }
            }
            _titulo.value = "${partido.equipoLocal?.nombre ?: "Local"} vs ${partido.equipoVisitante?.nombre ?: "Visitante"}"
            when (val r = repository.obtenerPlantilla()) {
                is Resultado.Ok -> {
                    val equipo = r.datos.equipo
                    tipoEquipo = when (equipo.id) {
                        partido.equipoLocal?.id -> "local"
                        partido.equipoVisitante?.id -> "visitante"
                        else -> null
                    }
                    if (tipoEquipo == null) {
                        _jugadoresDisponibles.value = emptyList()
                        _mensaje.value = "${equipo.nombre} no juega en esta jornada"
                    } else {
                        val disponibles = r.datos.jugadores.filter { it.activo }
                        _jugadoresDisponibles.value = disponibles
                        _jugadoresSeleccionados.value = if (conservarSeleccion) {
                            _jugadoresSeleccionados.value.orEmpty()
                                .filter { sel -> disponibles.any { it.id == sel.id } }
                                .toSet()
                        } else {
                            val guardados = if (tipoEquipo == "local") partido.alineacionLocal else partido.alineacionVisitante
                            disponibles.filter { it.id in guardados }.toSet()
                        }
                    }
                }
                is Resultado.Fallo -> _mensaje.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    fun toggleSeleccion(jugador: JugadorApi) {
        val actuales = _jugadoresSeleccionados.value?.toMutableSet() ?: mutableSetOf()
        if (actuales.any { it.id == jugador.id }) {
            actuales.removeAll { it.id == jugador.id }
        } else {
            if (actuales.size >= MAX_JUGADORES) {
                _mensaje.value = "Máximo $MAX_JUGADORES jugadores permitidos"
                return
            }
            actuales.add(jugador)
        }
        _jugadoresSeleccionados.value = actuales
    }

    fun guardarAlineacion(partidoId: String?) {
        val tipo = tipoEquipo
        if (partidoId == null || tipo == null) {
            _mensaje.value = "No se puede guardar: la jornada o el equipo no son válidos"
            return
        }
        val seleccionados = _jugadoresSeleccionados.value.orEmpty()
        if (seleccionados.size < MIN_JUGADORES) {
            _mensaje.value = "Se requieren al menos $MIN_JUGADORES jugadores para armar alineación"
            return
        }
        _cargando.value = true
        viewModelScope.launch {
            val r = repository.guardarAlineacion(partidoId, tipo, seleccionados.map { it.id })
            _cargando.value = false
            when (r) {
                is Resultado.Ok -> _guardadoExitoso.value = true
                is Resultado.Fallo -> {
                    _mensaje.value = r.mensaje
                    if (r.codigo == 409) cargarJugadoresHabilitados(partidoId, conservarSeleccion = true)
                }
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    private companion object {
        const val MIN_JUGADORES = 7
        const val MAX_JUGADORES = 11
    }
}
