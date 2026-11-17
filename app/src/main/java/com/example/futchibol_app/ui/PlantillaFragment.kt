package com.example.futchibol_app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futchibol_app.R
import com.example.futchibol_app.databinding.FragmentPlantillaBinding
import com.example.futchibol_app.databinding.ItemJugadorBinding
import com.example.futchibol_app.network.JugadorApi
import com.example.futchibol_app.ui.viewmodel.PlantillaViewModel
import java.util.Locale

class PlantillaFragment : Fragment() {

    private var _binding: FragmentPlantillaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PlantillaViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantillaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PlantillaViewModel::class.java]
        binding.rvJugadores.layoutManager = LinearLayoutManager(requireContext())

        observarViewModel()

        binding.btnRegistrarJugadorPlantilla.setOnClickListener {
            startActivity(Intent(requireContext(), NuevoJugadorActivity::class.java))
        }

        binding.btnFiltroTodos.setOnClickListener { viewModel.cambiarFiltro("TODOS") }
        binding.btnFiltroHabilitados.setOnClickListener { viewModel.cambiarFiltro("HABILITADOS") }
        binding.btnFiltroInhabilitados.setOnClickListener { viewModel.cambiarFiltro("INHABILITADOS") }

        binding.etBuscarJugador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.buscar(s?.toString() ?: "")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarJugadores()
    }

    private fun observarViewModel() {
        viewModel.listaCompleta.observe(viewLifecycleOwner) { lista ->
            val habilitados = lista.count { it.activo }
            val inhabilitados = lista.count { !it.activo }

            binding.tvTituloPlantilla.text = "${viewModel.nombreEquipo} - ${lista.size} jugadores"

            binding.btnFiltroTodos.text = "Todos ${lista.size}"
            binding.btnFiltroHabilitados.text = "Habilitados $habilitados"
            binding.btnFiltroInhabilitados.text = "Inhabilitados $inhabilitados"
        }

        viewModel.listaFiltrada.observe(viewLifecycleOwner) { listaFiltrada ->
            binding.rvJugadores.adapter = JugadorApiAdapter(listaFiltrada) { jugador ->
                val intent = Intent(requireContext(), FichaJugadorActivity::class.java)
                intent.putExtra("JUGADOR_ID", jugador.id)
                intent.putExtra("JUGADOR_NOMBRE", jugador.nombre)
                startActivity(intent)
            }
        }

        viewModel.mensajeError.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class JugadorApiAdapter(
        private val items: List<JugadorApi>,
        private val onClick: (JugadorApi) -> Unit
    ) : RecyclerView.Adapter<JugadorApiAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemJugadorBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemJugadorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val jugador = items[position]
            holder.binding.tvDorsalItem.text = String.format(Locale.US, "%02d", jugador.dorsal)
            holder.binding.tvNombreItem.text = jugador.nombre
            holder.binding.tvPosicionMotivoItem.text = jugador.posicion

            if (jugador.activo) {
                holder.binding.tvEstadoItem.text = "Habilitado"
                holder.binding.tvEstadoItem.setBackgroundResource(R.drawable.bg_badge_green)
            } else {
                holder.binding.tvEstadoItem.text = "Inhabilitado"
                holder.binding.tvEstadoItem.setBackgroundResource(R.drawable.bg_badge_red)
            }

            holder.itemView.setOnClickListener { onClick(jugador) }
        }

        override fun getItemCount() = items.size
    }
}
