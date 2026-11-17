package com.example.futchibol_app.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futchibol_app.databinding.DialogCrearPartidoBinding
import com.example.futchibol_app.databinding.FragmentPartidosBinding
import com.example.futchibol_app.databinding.ItemPartidoBinding
import com.example.futchibol_app.network.PartidoApi
import com.example.futchibol_app.ui.viewmodel.PartidosViewModel
import java.util.Calendar
import java.util.Locale

class PartidosFragment : Fragment() {

    private var _binding: FragmentPartidosBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PartidosViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPartidosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PartidosViewModel::class.java]

        binding.rvPartidos.layoutManager = LinearLayoutManager(requireContext())

        observarViewModel()

        binding.btnCrearPartido.setOnClickListener {
            mostrarDialogoCrearPartido()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarPartidos()
    }

    private fun observarViewModel() {
        viewModel.partidos.observe(viewLifecycleOwner) { partidos ->
            if (partidos.isEmpty()) {
                binding.tvSinPartidos.visibility = View.VISIBLE
                binding.rvPartidos.visibility = View.GONE
            } else {
                binding.tvSinPartidos.visibility = View.GONE
                binding.rvPartidos.visibility = View.VISIBLE
                binding.rvPartidos.adapter = PartidosAdapter(
                    items = partidos,
                    onClickAlineacion = { partido ->
                        val intent = Intent(requireContext(), AlineacionActivity::class.java)
                        intent.putExtra("PARTIDO_ID", partido.id)
                        startActivity(intent)
                    },
                    onClickEliminar = { partido ->
                        confirmarEliminarPartido(partido)
                    }
                )
            }
        }

        viewModel.creacionExitosa.observe(viewLifecycleOwner) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "Jornada creada correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.mensajeError.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }
    }

    private fun confirmarEliminarPartido(partido: PartidoApi) {
        val localNom = partido.equipoLocal?.nombre ?: "Equipo Local"
        val visNom = partido.equipoVisitante?.nombre ?: "Equipo Visitante"
        val nombre = "$localNom vs $visNom"

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Jornada")
            .setMessage("¿Estás seguro de que deseas eliminar la jornada de $nombre?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarPartido(partido.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoCrearPartido() {
        val equipos = viewModel.equipos
        if (equipos.size < 2) {
            Toast.makeText(requireContext(), "Necesitas al menos dos equipos. Crea otro en la pestaña Equipos.", Toast.LENGTH_LONG).show()
            return
        }
        val dialogBinding = DialogCrearPartidoBinding.inflate(layoutInflater)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, equipos.map { it.nombre })
        dialogBinding.spEquipoLocal.adapter = adapter
        dialogBinding.spEquipoVisitante.adapter = adapter

        val iActual = equipos.indexOfFirst { it.id == viewModel.equipoActual?.id }.coerceAtLeast(0)
        dialogBinding.spEquipoLocal.setSelection(iActual)
        dialogBinding.spEquipoVisitante.setSelection(if (iActual == 0) 1 else 0)

        dialogBinding.etFechaPartido.setOnClickListener {
            mostrarDatePicker(dialogBinding.etFechaPartido)
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Crear") { _, _ ->
                viewModel.crearPartido(
                    equipos.getOrNull(dialogBinding.spEquipoLocal.selectedItemPosition),
                    equipos.getOrNull(dialogBinding.spEquipoVisitante.selectedItemPosition),
                    dialogBinding.etFechaPartido.text.toString().trim(),
                    dialogBinding.etLugarPartido.text.toString().trim()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val fechaFormateada = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            editText.setText(fechaFormateada)
        }, year, month, day).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class PartidosAdapter(
        private val items: List<PartidoApi>,
        private val onClickAlineacion: (PartidoApi) -> Unit,
        private val onClickEliminar: (PartidoApi) -> Unit
    ) : RecyclerView.Adapter<PartidosAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemPartidoBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemPartidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val partido = items[position]
            val localNom = partido.equipoLocal?.nombre ?: "Equipo Local"
            val visNom = partido.equipoVisitante?.nombre ?: "Equipo Visitante"

            holder.binding.tvFechaLugarPartido.text = "Fecha: ${partido.fechaPartido.take(10)} · Lugar: ${partido.lugar.ifBlank { "por definir" }}"
            holder.binding.tvEquiposPartido.text = "$localNom vs $visNom"

            holder.binding.btnAlineacionPartido.setOnClickListener {
                onClickAlineacion(partido)
            }

            holder.binding.btnEliminarPartido.setOnClickListener {
                onClickEliminar(partido)
            }
        }

        override fun getItemCount() = items.size
    }
}
