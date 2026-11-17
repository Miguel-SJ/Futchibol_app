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
import com.example.futchibol_app.data.Fechas
import com.example.futchibol_app.network.motivoLegible
import com.example.futchibol_app.network.periodo
import com.example.futchibol_app.network.estadoTexto

class FichaJugadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFichaJugadorBinding
    private val viewModel: FichaJugadorViewModel by viewModels()
    private var jugadorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFichaJugadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        jugadorId = intent.getStringExtra("JUGADOR_ID")
        binding.tvNombreFicha.text = intent.getStringExtra("JUGADOR_NOMBRE") ?: ""

        observarViewModel()

        binding.btnRegistrarIncidenciaFicha.setOnClickListener {
            startActivity(Intent(this, IncidenciaActivity::class.java))
        }
        binding.btnHabilitarJugador.setOnClickListener {
            jugadorId?.let { viewModel.resolverInhabilitacion(it) }
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
                binding.tvEquipoFicha.text = it.equipo?.nombre ?: ""
                binding.tvPosicionDorsalFicha.text = "${it.posicion} · Dorsal ${it.dorsal}"
                if (it.activo) {
                    binding.cvAlertaInhabilitado.visibility = View.GONE
                    binding.btnHabilitarJugador.visibility = View.GONE
                    binding.tvEstadoDisciplinario.text = "Sin sanciones activas. Jugador habilitado."
                } else {
                    val sancion = it.inhabilitacion
                    binding.cvAlertaInhabilitado.visibility = View.VISIBLE
                    binding.btnHabilitarJugador.visibility = View.VISIBLE
                    binding.tvDetalleAlerta.text = sancion?.let { s ->
                        "${s.motivoLegible()} · ${s.periodo()}"
                    } ?: "Sanción o lesión activa"
                    binding.tvEstadoDisciplinario.text =
                        "Inhabilitado. ${sancion?.descripcion ?: "Sanción o lesión activa."}"
                }
            }
        }

        viewModel.historial.observe(this) { lista ->
            val hoy = Fechas.hoy()
            binding.tvHistorialFicha.text = if (lista.isEmpty()) {
                "Sin sanciones registradas."
            } else {
                lista.joinToString("\n\n") { s ->
                    "• ${s.motivoLegible()} (${s.estadoTexto(hoy)})\n  ${s.periodo()}"
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
                viewModel.limpiarMensaje()
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
