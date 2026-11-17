package com.example.futchibol_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futchibol_app.data.FutbolRepository
import com.example.futchibol_app.data.Resultado
import com.example.futchibol_app.network.EquipoApi
import kotlinx.coroutines.launch

class EquiposViewModel(
    private val repository: FutbolRepository = FutbolRepository()
) : ViewModel() {

    private val _equipos = MutableLiveData<List<EquipoApi>>(emptyList())
    val equipos: LiveData<List<EquipoApi>> = _equipos

    private val _equipoActualId = MutableLiveData<String?>()
    val equipoActualId: LiveData<String?> = _equipoActualId

    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> = _mensaje

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    fun cargar() {
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.obtenerEquipos()) {
                is Resultado.Ok -> {
                    _equipos.value = r.datos
                    val actual = r.datos.firstOrNull { it.id == repository.equipoActualId() }
                    if (actual == null) repository.seleccionarEquipo(null)
                    _equipoActualId.value = actual?.id
                }
                is Resultado.Fallo -> _mensaje.value = r.mensaje
            }
            _cargando.value = false
        }
    }

    fun seleccionar(equipo: EquipoApi) {
        repository.seleccionarEquipo(equipo)
        _equipoActualId.value = equipo.id
        _mensaje.value = "Equipo actual: ${equipo.nombre}"
    }

    fun crear(nombre: String, capitan: String, contacto: String) {
        if (nombre.isBlank()) {
            _mensaje.value = "El nombre del equipo es obligatorio"
            return
        }
        _cargando.value = true
        viewModelScope.launch {
            when (val r = repository.crearEquipo(nombre, capitan, contacto)) {
                is Resultado.Ok -> {
                    _mensaje.value = "Equipo creado. Tócalo en la lista para elegirlo."
                    cargar()
                }
                is Resultado.Fallo -> {
                    _mensaje.value = r.mensaje
                    _cargando.value = false
                }
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
