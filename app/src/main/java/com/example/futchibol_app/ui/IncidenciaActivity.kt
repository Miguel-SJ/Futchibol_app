package com.example.futchibol_app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.futchibol_app.databinding.ActivityIncidenciaBinding
import com.example.futchibol_app.network.JugadorApi
import com.example.futchibol_app.ui.viewmodel.IncidenciaViewModel
import java.util.Calendar
import java.util.Locale

class IncidenciaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncidenciaBinding
    private val viewModel: IncidenciaViewModel by viewModels()

    private var listaJugadores: List<JugadorApi> = emptyList()

    private val motivosMap = mapOf(
        "Tarjeta Roja" to "tarjeta_roja",
        "Tarjeta Amarilla" to "tarjeta_amarilla",
        "Lesión" to "lesion",
        "Sanción Disciplinaria" to "sancion_disciplinaria",
        "Enfermedad" to "enfermedad"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarMotivosSpinner()
        configurarDatePicker()
        observarViewModel()

        viewModel.cargarJugadores()

        binding.btnGuardarIncidencia.setOnClickListener {
            ejecutarGuardado()
        }
    }

    private fun configurarMotivosSpinner() {
        val motivosNombres = motivosMap.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, motivosNombres)
        binding.spMotivoIncidencia.adapter = adapter
    }

    private fun configurarDatePicker() {
        binding.etFechaRegistro.setOnClickListener {
            mostrarDatePicker { fecha -> binding.etFechaRegistro.setText(fecha) }
        }
        binding.etFechaFin.setOnClickListener {
            mostrarDatePicker { fecha -> binding.etFechaFin.setText(fecha) }
        }
    }

    private fun mostrarDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val fechaFormateada = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(fechaFormateada)
        }, year, month, day).show()
    }

    private fun observarViewModel() {
        viewModel.jugadores.observe(this) { jugadores ->
            listaJugadores = jugadores
            val nombres = jugadores.map { "${String.format(Locale.US, "%02d", it.dorsal)} - ${it.nombre}" }
            val adapter = ArrayAdapter(this@IncidenciaActivity, android.R.layout.simple_spinner_dropdown_item, nombres)
            binding.spJugadorIncidencia.adapter = adapter
        }

        viewModel.exitoRegistro.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, "Registrada correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.mensajeError.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }
    }

    private fun ejecutarGuardado() {
        val posJugador = binding.spJugadorIncidencia.selectedItemPosition
        if (posJugador < 0 || listaJugadores.isEmpty()) {
            Toast.makeText(this, "Seleccione un jugador", Toast.LENGTH_SHORT).show()
            return
        }

        val jugador = listaJugadores[posJugador]
        val motivoNombre = binding.spMotivoIncidencia.selectedItem?.toString() ?: "Tarjeta Roja"
        val motivoCod = motivosMap[motivoNombre] ?: "tarjeta_roja"

        val fechaInicio = binding.etFechaRegistro.text.toString().trim()
        val fechaFin = binding.etFechaFin.text.toString().trim()
        val observaciones = binding.etObservaciones.text.toString().trim()

        viewModel.registrarIncidencia(
            jugadorId = jugador.id,
            motivo = motivoCod,
            descripcion = observaciones,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )
    }
}
