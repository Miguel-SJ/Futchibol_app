package com.example.futchibol_app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futchibol_app.R
import com.example.futchibol_app.databinding.FragmentInhabilitacionesBinding
import com.example.futchibol_app.databinding.ItemJugadorBinding
import com.example.futchibol_app.network.JugadorApi
import com.example.futchibol_app.ui.viewmodel.InhabilitacionesViewModel
import java.util.Locale

class InhabilitacionesFragment : Fragment() {

    private var _binding: FragmentInhabilitacionesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: InhabilitacionesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInhabilitacionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[InhabilitacionesViewModel::class.java]

        binding.rvInhabilitaciones.layoutManager = LinearLayoutManager(requireContext())

        observarViewModel()

        binding.btnRegistrarInhabilitacion.setOnClickListener {
            startActivity(Intent(requireContext(), IncidenciaActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarInhabilitados()
    }

    private fun observarViewModel() {
        viewModel.inhabilitados.observe(viewLifecycleOwner) { lista ->
            binding.rvInhabilitaciones.adapter = InhabilitadosAdapter(lista) { jugador ->
                val intent = Intent(requireContext(), FichaJugadorActivity::class.java)
                intent.putExtra("JUGADOR_ID", jugador.id)
                intent.putExtra("JUGADOR_NOMBRE", jugador.nombre)
                startActivity(intent)
            }
        }

        viewModel.mensajeError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class InhabilitadosAdapter(
        private val items: List<JugadorApi>,
        private val onClick: (JugadorApi) -> Unit
    ) : RecyclerView.Adapter<InhabilitadosAdapter.ViewHolder>() {

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
            holder.binding.tvEstadoItem.text = "Inhabilitado"
            holder.binding.tvEstadoItem.setBackgroundResource(R.drawable.bg_badge_red)

            holder.itemView.setOnClickListener { onClick(jugador) }
        }

        override fun getItemCount() = items.size
    }
}
