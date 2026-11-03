package com.example.futchibol_app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.futchibol_app.databinding.ActivityFichaJugadorBinding
import com.example.futchibol_app.ui.viewmodel.FichaJugadorViewModel

class FichaJugadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFichaJugadorBinding
    private val viewModel: FichaJugadorViewModel by viewModels()

    private var jugadorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFichaJugadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        jugadorId = intent.getStringExtra("JUGADOR_ID")
        val nombrePorDefecto = intent.getStringExtra("JUGADOR_NOMBRE") ?: ""

        binding.tvNombreFicha.text = nombrePorDefecto

        observarViewModel()

        jugadorId?.let { viewModel.cargarDetalle(it) }

        binding.btnRegistrarIncidenciaFicha.setOnClickListener {
            startActivity(Intent(this, IncidenciaActivity::class.java))
        }

        binding.btnHabilitarJugador.setOnClickListener {
            jugadorId?.let { id ->
                viewModel.resolverInhabilitacion(id)
            }
        }

        binding.btnEliminarJugador.setOnClickListener {
            confirmarEliminacion()
        }
    }

    override fun onResume() {
        super.onResume()
        jugadorId?.let { viewModel.cargarDetalle(it) }
    }

    private fun observarViewModel() {
        viewModel.jugadorDetalle.observe(this) { jugador ->
            jugador?.let {
                binding.tvNombreFicha.text = it.nombre
                binding.tvPosicionDorsalFicha.text = "${it.posicion} · Dorsal ${it.dorsal}"

                if (it.activo) {
                    binding.cvAlertaInhabilitado.visibility = View.GONE
                    binding.btnHabilitarJugador.visibility = View.GONE
                    binding.tvEstadoDisciplinario.text = "Sin sanciones activas. Jugador habilitado."
                } else {
                    binding.cvAlertaInhabilitado.visibility = View.VISIBLE
                    binding.btnHabilitarJugador.visibility = View.VISIBLE
                    binding.tvEstadoDisciplinario.text = "Inhabilitado. Sanción o lesión activa."
                }
            }
        }

        viewModel.resolucionExitosa.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, "Inhabilitación removida. Jugador habilitado.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.eliminacionExitosa.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, "Jugador eliminado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.mensajeError.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarEliminacion() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Jugador")
            .setMessage("¿Estás seguro de que deseas eliminar este jugador? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                jugadorId?.let { viewModel.eliminarJugador(it) }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
