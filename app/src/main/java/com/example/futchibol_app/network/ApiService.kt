package com.example.futchibol_app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/jugador")
    suspend fun obtenerJugadores(): Response<List<JugadorApi>>

    @GET("api/jugador")
    suspend fun obtenerJugadoresInhabilitados(
        @Query("status") status: String = "unavailable"
    ): Response<List<JugadorApi>>

    @GET("api/jugador/{id}")
    suspend fun obtenerDetalleJugador(
        @Path("id") id: String
    ): Response<JugadorApi>

    @POST("api/jugador/guardar-jugador")
    suspend fun guardarJugador(
        @Body request: GuardarJugadorRequest
    ): Response<JugadorApi>

    @PUT("api/jugador/editar-jugador/{id}")
    suspend fun editarJugador(
        @Path("id") id: String,
        @Body request: GuardarJugadorRequest
    ): Response<JugadorApi>

    @DELETE("api/jugador/eliminar-jugador/{id}")
    suspend fun eliminarJugador(
        @Path("id") id: String
    ): Response<Unit>

    @POST("api/inhabilitacion/guardar-inhabilitacion")
    suspend fun guardarInhabilitacion(
        @Body request: GuardarInhabilitacionRequest
    ): Response<Unit>

    @PATCH("api/inhabilitacion/resolver-inhabilitacion/{id}")
    suspend fun resolverInhabilitacion(
        @Path("id") id: String
    ): Response<Unit>

    @GET("api/partido")
    suspend fun obtenerPartidos(): Response<List<PartidoApi>>

    @POST("api/partido/guardar-partido")
    suspend fun guardarPartido(
        @Body request: GuardarPartidoRequest
    ): Response<Unit>

    @POST("api/partido/alineacion/{id}")
    suspend fun guardarAlineacion(
        @Path("id") partidoId: String,
        @Body request: GuardarAlineacionRequest
    ): Response<Unit>
}
