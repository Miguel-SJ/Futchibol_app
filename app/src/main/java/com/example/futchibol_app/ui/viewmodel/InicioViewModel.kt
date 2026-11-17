package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.data.Resultado
import com.example.futchibol_app.network.*
import kotlinx.coroutines.launch

data class ResumenInicioState(
    val nombreTorneo: String = "",
    val nombreEquipo: String = "",
    val proximoPartido: PartidoApi? = null,
    val habilitadosCount: Int = 0,
    val inhabilitadosCount: Int = 0,
    val mensajeInhabilitados: String = "",
    val cargando: Boolean = false
)

class InicioViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _uiState = MutableLiveData(ResumenInicioState())
    val uiState: LiveData<ResumenInicioState> = _uiState

    private val _evento = MutableLiveData<String?>()
    val evento: LiveData<String?> = _evento

    fun cargarResumenJugadores() {
        val actual = _uiState.value ?: ResumenInicioState()
        _uiState.value = actual.copy(cargando = true)
        viewModelScope.launch {
            val torneo = repository.nombreTorneo()
            val proximo = repository.proximoPartido(repository.obtenerPartidos().getOrNull().orEmpty())
            _uiState.value = when (val r = repository.obtenerPlantilla()) {
                is Resultado.Ok -> {
                    val inhabilitados = r.datos.jugadores.filter { !it.activo }
                    val mensaje = if (inhabilitados.isNotEmpty()) {
                        val nombres = inhabilitados.joinToString(", ") { it.nombre.substringBefore(" ") }
                        "$nombres no pueden jugar. Consulta el motivo."
                    } else {
                        "Todos los jugadores están disponibles para alinear."
                    }
                    ResumenInicioState(
                        nombreTorneo = torneo,
                        nombreEquipo = r.datos.equipo.nombre,
                        proximoPartido = proximo,
                        habilitadosCount = r.datos.jugadores.size - inhabilitados.size,
                        inhabilitadosCount = inhabilitados.size,
                        mensajeInhabilitados = mensaje
                    )
                }
                is Resultado.Fallo -> actual.copy(
                    nombreTorneo = torneo,
                    proximoPartido = proximo,
                    mensajeInhabilitados = r.mensaje,
                    cargando = false
                )
            }
        }
    }

    fun guardarTorneo(torneo: String) {
        if (torneo.isBlank()) {
            _evento.value = "Escribe el nombre del torneo"
            return
        }
        repository.guardarTorneo(torneo)
        _evento.value = "Torneo actualizado"
        cargarResumenJugadores()
    }

    fun limpiarEvento() {
        _evento.value = null
    }
}
