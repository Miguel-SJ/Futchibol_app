package com.example.futchibol_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.futchibol_app.data.AlmacenLocal
import com.example.futchibol_app.databinding.ActivityMainBinding
import com.example.futchibol_app.ui.EquiposFragment
import com.example.futchibol_app.ui.InicioFragment
import com.example.futchibol_app.ui.PlantillaFragment
import com.example.futchibol_app.ui.PartidosFragment
import com.example.futchibol_app.ui.InhabilitacionesFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val almacen by lazy { AlmacenLocal() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> cargarFragmento(InicioFragment())
                R.id.nav_equipos -> cargarFragmento(EquiposFragment())
                R.id.nav_equipo -> cargarFragmento(PlantillaFragment())
                R.id.nav_partidos -> cargarFragmento(PartidosFragment())
                R.id.nav_bajas -> cargarFragmento(InhabilitacionesFragment())
                else -> false
            }
        }

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId =
                if (almacen.equipoId == null) R.id.nav_equipos else R.id.nav_inicio
        }

        binding.tvEquipoActual.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_equipos
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarBanner()
    }

    fun actualizarBanner() {
        val nombre = almacen.equipoNombre
        binding.tvEquipoActual.text =
            if (nombre == null) "Sin equipo · toca para elegir uno" else "Equipo actual: $nombre"
    }

    private fun cargarFragmento(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}
