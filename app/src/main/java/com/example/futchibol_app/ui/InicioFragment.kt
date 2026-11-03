package com.example.futchibol_app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.futchibol_app.databinding.FragmentInicioBinding
import com.example.futchibol_app.ui.viewmodel.InicioViewModel
import java.util.Locale

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: InicioViewModel

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
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.tvContadorHabilitados.text = String.format(Locale.US, "%02d", state.habilitadosCount)
            binding.tvContadorInhabilitados.text = String.format(Locale.US, "%02d", state.inhabilitadosCount)
            binding.tvMensajeInhabilitados.text = state.mensajeInhabilitados
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
