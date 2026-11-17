package com.example.futchibol_app.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.futchibol_app.databinding.ActivityNuevoJugadorBinding
import com.example.futchibol_app.ui.viewmodel.NuevoJugadorViewModel

class NuevoJugadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoJugadorBinding
    private val viewModel: NuevoJugadorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoJugadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val posiciones = arrayOf("Portero", "Defensa", "Mediocampista", "Delantero")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, posiciones)
        binding.spPosicion.adapter = adapter

        observarViewModel()

        viewModel.cargarEquipo()

        binding.btnGuardarNuevoJugador.setOnClickListener {
            val nombre = binding.etNombreCompleto.text.toString().trim()
            val dorsalStr = binding.etDorsal.text.toString().trim()
            val posicion = binding.spPosicion.selectedItem.toString()

            viewModel.guardarJugador(nombre, dorsalStr, posicion)
        }

        binding.btnCancelarNuevoJugador.setOnClickListener {
            finish()
        }
    }

    private fun observarViewModel() {
        viewModel.nombreEquipo.observe(this) { binding.tvEquipoNuevoJugador.text = it }
        viewModel.exitoGuardar.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, "Registrado exitosamente", Toast.LENGTH_SHORT).show()
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
}
