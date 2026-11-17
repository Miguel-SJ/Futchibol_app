package com.example.futchibol_app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.futchibol_app.databinding.DialogEditarTorneoEquipoBinding
import com.example.futchibol_app.databinding.FragmentInicioBinding
import com.example.futchibol_app.network.PartidoApi
import com.example.futchibol_app.ui.viewmodel.InicioViewModel
import java.util.Locale

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: InicioViewModel
    private var partidoActual: PartidoApi? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[InicioViewModel::class.java]

        observarViewModel()

        binding.btnEditarTorneoEquipo.setOnClickListener {
            mostrarDialogoEditarTorneoEquipo()
        }

        binding.cardProximoPartido.setOnClickListener {
            partidoActual?.let { partido ->
                val intent = Intent(requireContext(), AlineacionActivity::class.java)
                intent.putExtra("PARTIDO_ID", partido.id)
                startActivity(intent)
            } ?: run {
                Toast.makeText(requireContext(), "Crea una nueva jornada en la sección Partidos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegistrarIncidenciaInicio.setOnClickListener {
            startActivity(Intent(requireContext(), IncidenciaActivity::class.java))
        }

        binding.btnAgregarJugadorInicio.setOnClickListener {
            startActivity(Intent(requireContext(), NuevoJugadorActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarResumenJugadores()
    }

    private fun observarViewModel() {
        viewModel.evento.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.limpiarEvento()
            }
        }
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.tvNombreTorneoYEquipo.text = "${state.nombreTorneo} ${state.nombreEquipo}"

            binding.tvContadorHabilitados.text = String.format(Locale.US, "%02d", state.habilitadosCount)
            binding.tvContadorInhabilitados.text = String.format(Locale.US, "%02d", state.inhabilitadosCount)
            binding.tvMensajeInhabilitados.text = state.mensajeInhabilitados

            partidoActual = state.proximoPartido
            if (partidoActual != null) {
                val p = partidoActual!!
                val localNom = p.equipoLocal?.nombre ?: "Equipo Local"
                val visNom = p.equipoVisitante?.nombre ?: "Equipo Visitante"

                binding.tvEquiposPartidoInicio.text = "$localNom vs $visNom"
                binding.tvFechaLugarPartidoInicio.text = "Fecha: ${p.fechaPartido.take(10)} · Lugar: ${p.lugar.ifBlank { "por definir" }}"
            } else {
                binding.tvEquiposPartidoInicio.text = "No hay partidos programados"
                binding.tvFechaLugarPartidoInicio.text = "Toca la pestaña Partidos para crear uno"
            }
        }
    }

    private fun mostrarDialogoEditarTorneoEquipo() {
        val dialogBinding = DialogEditarTorneoEquipoBinding.inflate(layoutInflater)
        val currentState = viewModel.uiState.value

        dialogBinding.etNombreTorneo.setText(currentState?.nombreTorneo ?: "")

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoTorneo = dialogBinding.etNombreTorneo.text.toString().trim()
                viewModel.guardarTorneo(nuevoTorneo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
