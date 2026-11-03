package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import kotlinx.coroutines.launch

data class ResumenInicioState(
    val habilitadosCount: Int = 0,
    val inhabilitadosCount: Int = 0,
    val mensajeInhabilitados: String = "",
    val error: String? = null,
    val cargando: Boolean = false
)

class InicioViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _uiState = MutableLiveData<ResumenInicioState>(ResumenInicioState())
    val uiState: LiveData<ResumenInicioState> = _uiState

    fun cargarResumenJugadores() {
        _uiState.value = _uiState.value?.copy(cargando = true, error = null)
        viewModelScope.launch {
            try {
                val respTodos = repository.obtenerJugadores()
                val respInhabilitados = repository.obtenerJugadoresInhabilitados()

                val todos = if (respTodos.isSuccessful && respTodos.body() != null) respTodos.body()!! else emptyList()
                val inhabilitadosDirectos = if (respInhabilitados.isSuccessful && respInhabilitados.body() != null) respInhabilitados.body()!! else emptyList()

                val inhabilitadosIds = inhabilitadosDirectos.map { it.id }.toSet()

                val totalInhabilitados = todos.filter { !it.activo || inhabilitadosIds.contains(it.id) }
                val totalHabilitados = todos.filter { it.activo && !inhabilitadosIds.contains(it.id) }

                val mensaje = if (totalInhabilitados.isNotEmpty()) {
                    val nombres = totalInhabilitados.joinToString(", ") { it.nombre.split(" ")[0] }
                    "$nombres no pueden jugar. Consulta el motivo."
                } else {
                    "Todos los jugadores están disponibles para alinear."
                }

                _uiState.value = ResumenInicioState(
                    habilitadosCount = totalHabilitados.size,
                    inhabilitadosCount = totalInhabilitados.size,
                    mensajeInhabilitados = mensaje,
                    cargando = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(
                    cargando = false,
                    mensajeInhabilitados = "Error al cargar la información."
                )
            }
        }
    }
}
