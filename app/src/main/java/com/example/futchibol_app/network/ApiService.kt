package com.example.futchibol_app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Equipos
    @GET("api/equipo")
    suspend fun obtenerEquipos(): Response<List<EquipoApi>>

    @POST("api/equipo/guardar-equipo")
    suspend fun guardarEquipo(@Body request: GuardarEquipoRequest): Response<EquipoGuardadoResponse>

    // Jugadores
    @GET("api/jugador")
    suspend fun obtenerJugadores(@Query("equipo") equipoId: String? = null): Response<List<JugadorApi>>

    @GET("api/jugador/{id}")
    suspend fun obtenerDetalleJugador(@Path("id") id: String): Response<JugadorApi>

    @POST("api/jugador/guardar-jugador")
    suspend fun guardarJugador(@Body request: GuardarJugadorRequest): Response<Unit>

    @DELETE("api/jugador/eliminar-jugador/{id}")
    suspend fun eliminarJugador(@Path("id") id: String): Response<Unit>

    // Inhabilitaciones
    @GET("api/inhabilitacion")
    suspend fun obtenerInhabilitaciones(): Response<List<InhabilitacionApi>>

    @POST("api/inhabilitacion/guardar-inhabilitacion")
    suspend fun guardarInhabilitacion(@Body request: GuardarInhabilitacionRequest): Response<Unit>

    @PATCH("api/inhabilitacion/resolver-inhabilitacion/{id}")
    suspend fun resolverInhabilitacion(@Path("id") inhabilitacionId: String): Response<Unit>

    // Partidos
    @GET("api/partido")
    suspend fun obtenerPartidos(@Query("equipo") equipoId: String? = null): Response<List<PartidoApi>>

    @GET("api/partido/{id}")
    suspend fun obtenerPartido(@Path("id") id: String): Response<PartidoApi>

    @DELETE("api/partido/eliminar-partido/{id}")
    suspend fun eliminarPartido(@Path("id") id: String): Response<Unit>

    @POST("api/partido/guardar-partido")
    suspend fun guardarPartido(@Body request: GuardarPartidoRequest): Response<PartidoResponse>

    @POST("api/partido/alineacion/{id}")
    suspend fun guardarAlineacion(
        @Path("id") partidoId: String,
        @Body request: GuardarAlineacionRequest
    ): Response<PartidoResponse>
}
