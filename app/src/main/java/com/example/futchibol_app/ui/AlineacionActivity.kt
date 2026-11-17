package com.example.futchibol_app.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futchibol_app.databinding.ActivityAlineacionBinding
import com.example.futchibol_app.databinding.ItemJugadorBinding
import com.example.futchibol_app.network.JugadorApi
import com.example.futchibol_app.ui.viewmodel.AlineacionViewModel
import java.util.Locale

class AlineacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlineacionBinding
    private val viewModel: AlineacionViewModel by viewModels()
    private var adapter: AlineacionAdapter? = null
    private var partidoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlineacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        partidoId = intent.getStringExtra("PARTIDO_ID")

        binding.rvSeleccionAlineacion.layoutManager = LinearLayoutManager(this)

        observarViewModel()

        viewModel.cargarJugadoresHabilitados(partidoId)

        binding.btnRegistrarAlineacion.setOnClickListener {
            viewModel.guardarAlineacion(partidoId)
        }
    }

    private fun observarViewModel() {
        viewModel.titulo.observe(this) { binding.tvTituloAlineacion.text = it }
        viewModel.jugadoresDisponibles.observe(this) { lista ->
            adapter = AlineacionAdapter(lista, viewModel.jugadoresSeleccionados.value ?: emptySet()) { jugador ->
                viewModel.toggleSeleccion(jugador)
            }
            binding.rvSeleccionAlineacion.adapter = adapter
        }

        viewModel.jugadoresSeleccionados.observe(this) { seleccionados ->
            adapter?.actualizarSeleccionados(seleccionados)
            actualizarCancha(seleccionados)
        }

        viewModel.guardadoExitoso.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, "Alineación guardada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.mensaje.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }
    }

    private fun actualizarCancha(seleccionados: Set<JugadorApi>) {
        val porteros = seleccionados.filter { it.posicion.equals("Portero", true) }
        val defensas = seleccionados.filter { it.posicion.equals("Defensa", true) }
        val medios = seleccionados.filter { it.posicion.contains("Medio", true) }
        val delanteros = seleccionados.filter { it.posicion.equals("Delantero", true) }

        binding.tvPorteros.text = porteros.joinToString("   ") { "${it.dorsal} ${it.nombre.substringBefore(" ")}" }
        binding.tvDefensas.text = defensas.joinToString("   ") { "${it.dorsal} ${it.nombre.substringBefore(" ")}" }
        binding.tvMediocampistas.text = medios.joinToString("   ") { "${it.dorsal} ${it.nombre.substringBefore(" ")}" }
        binding.tvDelanteros.text = delanteros.joinToString("   ") { "${it.dorsal} ${it.nombre.substringBefore(" ")}" }

        binding.tvEstadoAlineacion.text = "Seleccionados: ${seleccionados.size}/11"
    }

    private class AlineacionAdapter(
        private val items: List<JugadorApi>,
        private var seleccionados: Set<JugadorApi>,
        private val onClick: (JugadorApi) -> Unit
    ) : RecyclerView.Adapter<AlineacionAdapter.ViewHolder>() {

        fun actualizarSeleccionados(nuevosSeleccionados: Set<JugadorApi>) {
            this.seleccionados = nuevosSeleccionados
            notifyDataSetChanged()
        }

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

            val estaSeleccionado = seleccionados.any { it.id == jugador.id }
            if (estaSeleccionado) {
                holder.itemView.setBackgroundColor(Color.parseColor("#E0F2F1"))
                holder.binding.tvEstadoItem.text = "SELECCIONADO"
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                holder.binding.tvEstadoItem.text = "Tocar para elegir"
            }

            holder.itemView.setOnClickListener { onClick(jugador) }
        }

        override fun getItemCount() = items.size
    }
}
