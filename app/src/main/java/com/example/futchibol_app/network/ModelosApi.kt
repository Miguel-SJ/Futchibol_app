package com.example.futchibol_app.network

import com.google.gson.annotations.SerializedName

data class EquipoApi(
    @SerializedName("_id") val id: String,
    @SerializedName("nombre") val nombre: String
)

data class JugadorApi(
    @SerializedName("_id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("dorsal") val dorsal: Int,
    @SerializedName("posicion") val posicion: String,
    @SerializedName("equipo") val equipo: EquipoApi?,
    @SerializedName("activo") val activo: Boolean
)

data class GuardarJugadorRequest(
    val nombre: String,
    val dorsal: Int,
    val posicion: String,
    val equipo: String
)

data class GuardarInhabilitacionRequest(
    val jugador: String,
    val motivo: String,
    val descripcion: String,
    val fechaInicio: String,
    val fechaFin: String
)

data class PartidoApi(
    @SerializedName("_id") val id: String,
    @SerializedName("equipoLocal") val equipoLocal: EquipoApi,
    @SerializedName("equipoVisitante") val equipoVisitante: EquipoApi,
    @SerializedName("fechaPartido") val fechaPartido: String,
    @SerializedName("lugar") val lugar: String
)

data class GuardarPartidoRequest(
    val equipoLocal: String,
    val equipoVisitante: String,
    val fechaPartido: String,
    val lugar: String
)

data class GuardarAlineacionRequest(
    val tipoEquipo: String,
    val jugadoresIds: List<String>
)