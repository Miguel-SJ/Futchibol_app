package com.example.futchibol_app.data

import com.example.futchibol_app.network.ApiService
import com.example.futchibol_app.network.ErrorApi
import com.example.futchibol_app.network.EquipoApi
import com.example.futchibol_app.network.GuardarAlineacionRequest
import com.example.futchibol_app.network.GuardarEquipoRequest
import com.example.futchibol_app.network.GuardarInhabilitacionRequest
import com.example.futchibol_app.network.GuardarJugadorRequest
import com.example.futchibol_app.network.GuardarPartidoRequest
import com.example.futchibol_app.network.InhabilitacionApi
import com.example.futchibol_app.network.JugadorApi
import com.example.futchibol_app.network.PartidoApi
import com.example.futchibol_app.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.Response
import java.io.IOException

data class Plantilla(val equipo: EquipoApi, val jugadores: List<JugadorApi>)
data class FichaJugador(val jugador: JugadorApi, val historial: List<InhabilitacionApi>)

class FutbolRepository(
    private val api: ApiService = RetrofitClient.apiService,
    private val almacen: AlmacenLocal = AlmacenLocal()
) {
    private val gson = Gson()

    // red

    private suspend fun <T> llamar(bloque: suspend () -> Response<T>): Resultado<T> = try {
        val respuesta = bloque()
        val cuerpo = respuesta.body()
        when {
            !respuesta.isSuccessful -> Resultado.Fallo(mensajeError(respuesta), respuesta.code())
            cuerpo == null -> Resultado.Fallo("El servidor devolvió una respuesta vacía", respuesta.code())
            else -> Resultado.Ok(cuerpo)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Resultado.Fallo("Sin conexión con el servidor. Revisa tu internet e intenta de nuevo.")
    } catch (e: Exception) {
        Resultado.Fallo("Respuesta inesperada del servidor")
    }

    private suspend fun <T> llamarLista(bloque: suspend () -> Response<List<T>>): Resultado<List<T>> {
        val r = llamar(bloque)
        return if (r is Resultado.Fallo && r.codigo == 404) Resultado.Ok(emptyList()) else r
    }

    private fun <T> mensajeError(respuesta: Response<T>): String {
        val cuerpo = try { respuesta.errorBody()?.string() } catch (e: Exception) { null }
        val mensaje = try { gson.fromJson(cuerpo, ErrorApi::class.java)?.message } catch (e: Exception) { null }
        return mensaje ?: "Error del servidor (${respuesta.code()})"
    }

    // configuración local

    fun nombreTorneo(): String = almacen.nombreTorneo

    fun guardarTorneo(nombre: String) {
        almacen.nombreTorneo = nombre.trim()
    }

    // equipos

    suspend fun obtenerEquipos(): Resultado<List<EquipoApi>> = llamarLista { api.obtenerEquipos() }

    fun hayEquipoElegido(): Boolean = almacen.equipoId != null

    fun equipoActualId(): String? = almacen.equipoId

    suspend fun equipoActual(): Resultado<EquipoApi> {
        val id = almacen.equipoId ?: return Resultado.Fallo(MSG_SIN_EQUIPO)
        val equipos = when (val r = obtenerEquipos()) {
            is Resultado.Ok -> r.datos
            is Resultado.Fallo -> return r
        }
        val actual = equipos.firstOrNull { it.id == id }
        if (actual == null) {
            seleccionarEquipo(null)
            return Resultado.Fallo(MSG_SIN_EQUIPO)
        }
        almacen.equipoNombre = actual.nombre
        return Resultado.Ok(actual)
    }

    fun seleccionarEquipo(equipo: EquipoApi?) {
        almacen.equipoId = equipo?.id
        almacen.equipoNombre = equipo?.nombre
    }

    suspend fun crearEquipo(nombre: String, capitan: String, contacto: String): Resultado<EquipoApi> {
        val req = GuardarEquipoRequest(nombre.trim(), capitan.trim().ifEmpty { null }, contacto.trim().ifEmpty { null })
        return when (val r = llamar { api.guardarEquipo(req) }) {
            is Resultado.Ok -> r.datos.equipo?.let { Resultado.Ok(it) } ?: Resultado.Fallo("No se pudo crear el equipo")
            is Resultado.Fallo -> r
        }
    }

    // jugadores e inhabilitaciones

    suspend fun obtenerPlantilla(): Resultado<Plantilla> {
        val equipo = when (val r = equipoActual()) {
            is Resultado.Ok -> r.datos
            is Resultado.Fallo -> return r
        }
        return coroutineScope<Resultado<Plantilla>> {
            val jugadores = async { llamarLista { api.obtenerJugadores(equipo.id) } }
            val sanciones = async { llamarLista { api.obtenerInhabilitaciones() } }
            val rj = jugadores.await()
            val rs = sanciones.await()
            when (rj) {
                is Resultado.Fallo -> rj
                is Resultado.Ok -> when (rs) {
                    is Resultado.Fallo -> rs
                    is Resultado.Ok -> Resultado.Ok(Plantilla(equipo, aplicarSanciones(rj.datos, rs.datos)))
                }
            }
        }
    }

    private fun aplicarSanciones(jugadores: List<JugadorApi>, sanciones: List<InhabilitacionApi>): List<JugadorApi> {
        val activasPorJugador = sanciones
            .filter { !it.resuelta }
            .mapNotNull { s -> s.jugador?.let { it.id to s } }
            .groupBy({ it.first }, { it.second })
        return jugadores.map { jugador ->
            val sancion = activasPorJugador[jugador.id]?.maxByOrNull { it.fechaFin ?: "9999" }
            jugador.copy(activo = sancion == null, inhabilitacion = sancion)
        }
    }

    suspend fun obtenerFicha(id: String): Resultado<FichaJugador> = coroutineScope<Resultado<FichaJugador>> {
        val detalle = async { llamar { api.obtenerDetalleJugador(id) } }
        val sanciones = async { llamarLista { api.obtenerInhabilitaciones() } }
        val rj = detalle.await()
        val rs = sanciones.await()
        when (rj) {
            is Resultado.Fallo -> rj
            is Resultado.Ok -> when (rs) {
                is Resultado.Fallo -> rs
                is Resultado.Ok -> {
                    val historial = rs.datos
                        .filter { it.jugador?.id == id }
                        .sortedByDescending { it.fechaInicio ?: "" }
                    val activa = historial.firstOrNull { !it.resuelta }
                    Resultado.Ok(FichaJugador(rj.datos.copy(activo = activa == null, inhabilitacion = activa), historial))
                }
            }
        }
    }

    suspend fun guardarJugador(nombre: String, dorsal: Int, posicion: String): Resultado<Unit> {
        val equipo = when (val r = equipoActual()) {
            is Resultado.Ok -> r.datos
            is Resultado.Fallo -> return r
        }
        return llamar { api.guardarJugador(GuardarJugadorRequest(nombre, dorsal, posicion, equipo.id)) }
    }

    suspend fun eliminarJugador(id: String): Resultado<Unit> = llamar { api.eliminarJugador(id) }

    suspend fun guardarInhabilitacion(request: GuardarInhabilitacionRequest): Resultado<Unit> =
        llamar { api.guardarInhabilitacion(request) }

    suspend fun resolverInhabilitaciones(jugadorId: String): Resultado<Unit> {
        val activas = when (val r = sancionesActivasDe(jugadorId)) {
            is Resultado.Ok -> r.datos
            is Resultado.Fallo -> return r
        }
        for (sancion in activas) {
            val r = llamar { api.resolverInhabilitacion(sancion.id) }
            if (r is Resultado.Fallo) return r
        }
        return Resultado.Ok(Unit)
    }

    private suspend fun sancionesActivasDe(jugadorId: String): Resultado<List<InhabilitacionApi>> =
        when (val r = llamarLista { api.obtenerInhabilitaciones() }) {
            is Resultado.Ok -> Resultado.Ok(r.datos.filter { !it.resuelta && it.jugador?.id == jugadorId })
            is Resultado.Fallo -> r
        }

    // partidos

    suspend fun obtenerPartidos(): Resultado<List<PartidoApi>> {
        val id = almacen.equipoId ?: return Resultado.Fallo(MSG_SIN_EQUIPO)
        return when (val r = llamarLista { api.obtenerPartidos(id) }) {
            is Resultado.Ok -> Resultado.Ok(r.datos.sortedBy { it.fechaPartido })
            is Resultado.Fallo -> r
        }
    }

    suspend fun obtenerPartido(id: String): Resultado<PartidoApi> = llamar { api.obtenerPartido(id) }

    fun proximoPartido(partidos: List<PartidoApi>): PartidoApi? {
        val hoy = Fechas.hoy()
        return partidos.firstOrNull { it.fechaPartido.take(10) >= hoy } ?: partidos.lastOrNull()
    }

    suspend fun crearPartido(localId: String, visitanteId: String, fecha: String, lugar: String): Resultado<Unit> {
        if (localId == visitanteId) return Resultado.Fallo("El equipo local y el visitante deben ser distintos")
        return when (val r = llamar { api.guardarPartido(GuardarPartidoRequest(localId, visitanteId, fecha, lugar)) }) {
            is Resultado.Ok -> Resultado.Ok(Unit)
            is Resultado.Fallo -> r
        }
    }

    suspend fun eliminarPartido(id: String): Resultado<Unit> = llamar { api.eliminarPartido(id) }

    suspend fun guardarAlineacion(partidoId: String, tipoEquipo: String, jugadoresIds: List<String>): Resultado<Unit> =
        when (val r = llamar { api.guardarAlineacion(partidoId, GuardarAlineacionRequest(tipoEquipo, jugadoresIds)) }) {
            is Resultado.Ok -> Resultado.Ok(Unit)
            is Resultado.Fallo -> r
        }

    private companion object {
        const val MSG_SIN_EQUIPO = "Aún no has elegido un equipo. Ve a la pestaña Equipos."
    }
}
