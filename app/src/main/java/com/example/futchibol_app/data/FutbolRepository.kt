package com.example.futchibol_app.data

import android.content.Context
import com.example.futchibol_app.FutbolApp
import com.example.futchibol_app.network.EquipoApi
import com.example.futchibol_app.network.GuardarAlineacionRequest
import com.example.futchibol_app.network.GuardarInhabilitacionRequest
import com.example.futchibol_app.network.GuardarJugadorRequest
import com.example.futchibol_app.network.GuardarPartidoRequest
import com.example.futchibol_app.network.JugadorApi
import com.example.futchibol_app.network.PartidoApi
import com.example.futchibol_app.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response
import java.util.UUID

class FutbolRepository {

    private val apiService = RetrofitClient.apiService
    private val gson = Gson()

    private val prefs by lazy {
        FutbolApp.context.getSharedPreferences("futbol_app_prefs_v1", Context.MODE_PRIVATE)
    }

    private companion object {
        private const val KEY_PARTIDOS = "key_partidos"
        private const val KEY_PARTIDOS_ELIMINADOS = "key_partidos_eliminados"
        private const val KEY_JUGADORES_CREADOS = "key_jugadores_creados"
        private const val KEY_JUGADORES_ELIMINADOS = "key_jugadores_eliminados"
        private const val KEY_INH_CREADAS = "key_inh_creadas"
        private const val KEY_INH_RESUELTAS = "key_inh_resueltas"
        private const val KEY_ALINEACIONES = "key_alineaciones"
    }

    private fun obtenerPartidosLocales(): MutableList<PartidoApi> {
        val json = prefs.getString(KEY_PARTIDOS, null)
        if (json.isNullOrEmpty()) {
            val iniciales = mutableListOf(
                PartidoApi(
                    id = "jornada_1",
                    equipoLocal = EquipoApi(Constants.DEFAULT_EQUIPO_ID, "Alianza Bardera"),
                    equipoVisitante = EquipoApi("visitante_1", "Lisiados Team"),
                    fechaPartido = "2026-11-08",
                    lugar = "Cancha Principal"
                )
            )
            guardarPartidosLocales(iniciales)
            return iniciales
        }
        val type = object : TypeToken<MutableList<PartidoApi>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun guardarPartidosLocales(lista: List<PartidoApi>) {
        prefs.edit().putString(KEY_PARTIDOS, gson.toJson(lista)).apply()
    }

    private fun obtenerPartidosEliminadosSet(): MutableSet<String> {
        return prefs.getStringSet(KEY_PARTIDOS_ELIMINADOS, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun guardarPartidosEliminadosSet(set: Set<String>) {
        prefs.edit().putStringSet(KEY_PARTIDOS_ELIMINADOS, set).apply()
    }

    private fun obtenerJugadoresCreadosLocales(): MutableList<JugadorApi> {
        val json = prefs.getString(KEY_JUGADORES_CREADOS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<JugadorApi>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun guardarJugadoresCreadosLocales(lista: List<JugadorApi>) {
        prefs.edit().putString(KEY_JUGADORES_CREADOS, gson.toJson(lista)).apply()
    }

    private fun obtenerJugadoresEliminadosSet(): MutableSet<String> {
        return prefs.getStringSet(KEY_JUGADORES_ELIMINADOS, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun guardarJugadoresEliminadosSet(set: Set<String>) {
        prefs.edit().putStringSet(KEY_JUGADORES_ELIMINADOS, set).apply()
    }

    // --- Persistencia de Inhabilitaciones ---
    private fun obtenerInhabilitacionesCreadasSet(): MutableSet<String> {
        return prefs.getStringSet(KEY_INH_CREADAS, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun guardarInhabilitacionesCreadasSet(set: Set<String>) {
        prefs.edit().putStringSet(KEY_INH_CREADAS, set).apply()
    }

    private fun obtenerInhabilitacionesResueltasSet(): MutableSet<String> {
        return prefs.getStringSet(KEY_INH_RESUELTAS, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun guardarInhabilitacionesResueltasSet(set: Set<String>) {
        prefs.edit().putStringSet(KEY_INH_RESUELTAS, set).apply()
    }

    private fun obtenerAlineacionesMap(): MutableMap<String, Set<String>> {
        val json = prefs.getString(KEY_ALINEACIONES, null) ?: return mutableMapOf()
        val type = object : TypeToken<MutableMap<String, Set<String>>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }

    private fun guardarAlineacionesMap(map: Map<String, Set<String>>) {
        prefs.edit().putString(KEY_ALINEACIONES, gson.toJson(map)).apply()
    }


    suspend fun obtenerJugadores(): Response<List<JugadorApi>> {
        val eliminados = obtenerJugadoresEliminadosSet()
        val inhCreadas = obtenerInhabilitacionesCreadasSet()
        val inhResueltas = obtenerInhabilitacionesResueltasSet()
        val creadosLocales = obtenerJugadoresCreadosLocales()

        return try {
            val response = apiService.obtenerJugadores()
            val remotos = if (response.isSuccessful && response.body() != null) response.body()!! else emptyList()
            val combinados = (remotos + creadosLocales).distinctBy { it.id }

            val filtradosYProcesados = combinados
                .filter { !eliminados.contains(it.id) }
                .map { jugador ->
                    when {
                        inhResueltas.contains(jugador.id) -> jugador.copy(activo = true)
                        inhCreadas.contains(jugador.id) -> jugador.copy(activo = false)
                        else -> jugador
                    }
                }

            Response.success(filtradosYProcesados)
        } catch (e: Exception) {
            val filtradosYProcesados = creadosLocales
                .filter { !eliminados.contains(it.id) }
                .map { jugador ->
                    when {
                        inhResueltas.contains(jugador.id) -> jugador.copy(activo = true)
                        inhCreadas.contains(jugador.id) -> jugador.copy(activo = false)
                        else -> jugador
                    }
                }
            Response.success(filtradosYProcesados)
        }
    }

    suspend fun obtenerJugadoresInhabilitados(): Response<List<JugadorApi>> {
        val respTodos = obtenerJugadores()
        if (respTodos.isSuccessful && respTodos.body() != null) {
            val inhabilitados = respTodos.body()!!.filter { !it.activo }
            return Response.success(inhabilitados)
        }
        return Response.success(emptyList())
    }

    suspend fun obtenerDetalleJugador(id: String): Response<JugadorApi> {
        val eliminados = obtenerJugadoresEliminadosSet()
        if (eliminados.contains(id)) {
            return Response.error(404, okhttp3.ResponseBody.create(null, ""))
        }

        val todos = obtenerJugadores().body() ?: emptyList()
        val enMemoria = todos.find { it.id == id }
        if (enMemoria != null) {
            return Response.success(enMemoria)
        }

        return try {
            val response = apiService.obtenerDetalleJugador(id)
            if (response.isSuccessful && response.body() != null) {
                var jugador = response.body()!!
                val inhResueltas = obtenerInhabilitacionesResueltasSet()
                val inhCreadas = obtenerInhabilitacionesCreadasSet()

                if (inhResueltas.contains(id)) {
                    jugador = jugador.copy(activo = true)
                } else if (inhCreadas.contains(id)) {
                    jugador = jugador.copy(activo = false)
                }
                Response.success(jugador)
            } else {
                response
            }
        } catch (e: Exception) {
            Response.error(404, okhttp3.ResponseBody.create(null, ""))
        }
    }

    suspend fun guardarJugador(request: GuardarJugadorRequest): Response<JugadorApi> {
        val nuevo = JugadorApi(
            id = UUID.randomUUID().toString(),
            nombre = request.nombre,
            dorsal = request.dorsal,
            posicion = request.posicion,
            equipo = EquipoApi(request.equipo, "Alianza Bardera"),
            activo = true
        )

        val creados = obtenerJugadoresCreadosLocales()
        creados.add(0, nuevo)
        guardarJugadoresCreadosLocales(creados)

        return try {
            apiService.guardarJugador(request)
            Response.success(nuevo)
        } catch (e: Exception) {
            Response.success(nuevo)
        }
    }

    suspend fun eliminarJugador(id: String): Response<Unit> {
        val eliminados = obtenerJugadoresEliminadosSet()
        eliminados.add(id)
        guardarJugadoresEliminadosSet(eliminados)

        val inhCreadas = obtenerInhabilitacionesCreadasSet()
        inhCreadas.remove(id)
        guardarInhabilitacionesCreadasSet(inhCreadas)

        val inhResueltas = obtenerInhabilitacionesResueltasSet()
        inhResueltas.remove(id)
        guardarInhabilitacionesResueltasSet(inhResueltas)

        val creados = obtenerJugadoresCreadosLocales()
        creados.removeAll { it.id == id }
        guardarJugadoresCreadosLocales(creados)

        return try {
            apiService.eliminarJugador(id)
        } catch (e: Exception) {
            Response.success(Unit)
        }
    }

    suspend fun guardarInhabilitacion(request: GuardarInhabilitacionRequest): Response<Unit> {
        val inhCreadas = obtenerInhabilitacionesCreadasSet()
        inhCreadas.add(request.jugador)
        guardarInhabilitacionesCreadasSet(inhCreadas)

        val inhResueltas = obtenerInhabilitacionesResueltasSet()
        inhResueltas.remove(request.jugador)
        guardarInhabilitacionesResueltasSet(inhResueltas)

        return try {
            apiService.guardarInhabilitacion(request)
        } catch (e: Exception) {
            Response.success(Unit)
        }
    }

    suspend fun resolverInhabilitacion(id: String): Response<Unit> {
        val inhResueltas = obtenerInhabilitacionesResueltasSet()
        inhResueltas.add(id)
        guardarInhabilitacionesResueltasSet(inhResueltas)

        val inhCreadas = obtenerInhabilitacionesCreadasSet()
        inhCreadas.remove(id)
        guardarInhabilitacionesCreadasSet(inhCreadas)

        return try {
            apiService.resolverInhabilitacion(id)
        } catch (e: Exception) {
            Response.success(Unit)
        }
    }

    suspend fun obtenerPartidos(): Response<List<PartidoApi>> {
        val eliminados = obtenerPartidosEliminadosSet()
        val locales = obtenerPartidosLocales().filter { !eliminados.contains(it.id) }
        return try {
            val response = apiService.obtenerPartidos()
            val remotos = if (response.isSuccessful && response.body() != null) response.body()!! else emptyList()
            val unificados = (remotos + locales)
                .distinctBy { it.id }
                .filter { !eliminados.contains(it.id) }
            Response.success(unificados)
        } catch (e: Exception) {
            Response.success(locales)
        }
    }

    suspend fun guardarPartido(request: GuardarPartidoRequest): Response<Unit> {
        val nuevoId = UUID.randomUUID().toString()
        val nuevoPartido = PartidoApi(
            id = nuevoId,
            equipoLocal = EquipoApi(Constants.DEFAULT_EQUIPO_ID, request.equipoLocal),
            equipoVisitante = EquipoApi("vis_$nuevoId", request.equipoVisitante),
            fechaPartido = request.fechaPartido,
            lugar = request.lugar
        )
        val locales = obtenerPartidosLocales()
        locales.add(0, nuevoPartido)
        guardarPartidosLocales(locales)

        return try {
            apiService.guardarPartido(request)
        } catch (e: Exception) {
            Response.success(Unit)
        }
    }

    suspend fun eliminarPartido(partidoId: String): Response<Unit> {
        val eliminados = obtenerPartidosEliminadosSet()
        eliminados.add(partidoId)
        guardarPartidosEliminadosSet(eliminados)

        val locales = obtenerPartidosLocales()
        locales.removeAll { it.id == partidoId }
        guardarPartidosLocales(locales)

        return Response.success(Unit)
    }

    suspend fun guardarAlineacion(partidoId: String, request: GuardarAlineacionRequest): Response<Unit> {
        val map = obtenerAlineacionesMap()
        map[partidoId] = request.jugadoresIds.toSet()
        guardarAlineacionesMap(map)

        return try {
            apiService.guardarAlineacion(partidoId, request)
        } catch (e: Exception) {
            Response.success(Unit)
        }
    }

    fun obtenerAlineacionGuardadaIds(partidoId: String): Set<String> {
        return obtenerAlineacionesMap()[partidoId] ?: emptySet()
    }
}