package com.example.futchibol_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futchibol_app.MainActivity
import com.example.futchibol_app.databinding.DialogCrearEquipoBinding
import com.example.futchibol_app.databinding.FragmentEquiposBinding
import com.example.futchibol_app.databinding.ItemEquipoBinding
import com.example.futchibol_app.network.EquipoApi
import com.example.futchibol_app.ui.viewmodel.EquiposViewModel

class EquiposFragment : Fragment() {

    private var _binding: FragmentEquiposBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EquiposViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquiposBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[EquiposViewModel::class.java]
        binding.rvEquipos.layoutManager = LinearLayoutManager(requireContext())

        observarViewModel()

        binding.btnCrearEquipo.setOnClickListener { mostrarDialogoCrearEquipo() }
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargar()
    }

    private fun observarViewModel() {
        val pintar = {
            val equipos = viewModel.equipos.value.orEmpty()
            binding.tvSinEquipos.visibility = if (equipos.isEmpty()) View.VISIBLE else View.GONE
            binding.rvEquipos.adapter = EquiposAdapter(equipos, viewModel.equipoActualId.value) { equipo ->
                viewModel.seleccionar(equipo)
                (activity as? MainActivity)?.actualizarBanner()
            }
        }
        viewModel.equipos.observe(viewLifecycleOwner) { pintar() }
        viewModel.equipoActualId.observe(viewLifecycleOwner) {
            pintar()
            (activity as? MainActivity)?.actualizarBanner()
        }

        viewModel.mensaje.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }
    }

    private fun mostrarDialogoCrearEquipo() {
        val dialogBinding = DialogCrearEquipoBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Crear") { _, _ ->
                viewModel.crear(
                    dialogBinding.etNombreEquipoNuevo.text.toString(),
                    dialogBinding.etCapitanEquipo.text.toString(),
                    dialogBinding.etContactoEquipo.text.toString()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class EquiposAdapter(
        private val items: List<EquipoApi>,
        private val actualId: String?,
        private val onElegir: (EquipoApi) -> Unit
    ) : RecyclerView.Adapter<EquiposAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemEquipoBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(ItemEquipoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val equipo = items[position]
            val esActual = equipo.id == actualId
            holder.binding.tvNombreEquipoItem.text = equipo.nombre
            holder.binding.tvDetalleEquipoItem.text = listOfNotNull(
                equipo.capitan?.takeIf { it.isNotBlank() }?.let { "Capitán: $it" },
                equipo.contacto?.takeIf { it.isNotBlank() }
            ).joinToString(" · ").ifEmpty { "Sin capitán ni contacto" }

            holder.binding.tvBadgeActual.visibility = if (esActual) View.VISIBLE else View.GONE
            holder.binding.btnElegirEquipo.visibility = if (esActual) View.GONE else View.VISIBLE
            holder.binding.btnElegirEquipo.setOnClickListener { onElegir(equipo) }
            holder.itemView.setOnClickListener { if (!esActual) onElegir(equipo) }
        }

        override fun getItemCount() = items.size
    }
}
