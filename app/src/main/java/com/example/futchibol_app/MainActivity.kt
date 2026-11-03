package com.example.futchibol_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.futchibol_app.databinding.ActivityMainBinding
import com.example.futchibol_app.ui.InicioFragment
import com.example.futchibol_app.ui.PlantillaFragment
import com.example.futchibol_app.ui.PartidosFragment
import com.example.futchibol_app.ui.InhabilitacionesFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            cargarFragmento(InicioFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> cargarFragmento(InicioFragment())
                R.id.nav_equipo -> cargarFragmento(PlantillaFragment())
                R.id.nav_partidos -> cargarFragmento(PartidosFragment())
                R.id.nav_bajas -> cargarFragmento(InhabilitacionesFragment())
                else -> false
            }
        }
    }

    private fun cargarFragmento(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}