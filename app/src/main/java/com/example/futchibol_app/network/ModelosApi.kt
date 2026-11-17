package com.example.futchibol_app.network

import com.google.gson.annotations.SerializedName

// Modelos

data class EquipoApi(
    @SerializedName("_id") val id: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("capitan") val capitan: String? = null,
    @SerializedName("contacto") val contacto: String? = null
)

data class JugadorApi(
    @SerializedName("_id") val id: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("dorsal") val dorsal: Int = 0,
    @SerializedName("posicion") val posicion: String = "Jugador",
    @SerializedName("equipo") val equipo: EquipoApi? = null,
    @SerializedName("activo") val activo: Boolean = true,
    @Transient val inhabilitacion: InhabilitacionApi? = null
)

data class JugadorRefApi(
    @SerializedName("_id") val id: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("dorsal") val dorsal: Int = 0
)

data class InhabilitacionApi(
    @SerializedName("_id") val id: String = "",
    @SerializedName("jugador") val jugador: JugadorRefApi? = null,
    @SerializedName("motivo") val motivo: String = "",
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("fechaInicio") val fechaInicio: String? = null,
    @SerializedName("fechaFin") val fechaFin: String? = null,
    @SerializedName("resuelta") val resuelta: Boolean = false
)

data class PartidoApi(
    @SerializedName("_id") val id: String = "",
    @SerializedName("equipoLocal") val equipoLocal: EquipoApi? = null,
    @SerializedName("equipoVisitante") val equipoVisitante: EquipoApi? = null,
    @SerializedName("fechaPartido") val fechaPartido: String = "",
    @SerializedName("lugar") val lugar: String = "",
    @SerializedName("alineacionLocal") val alineacionLocal: List<String> = emptyList(),
    @SerializedName("alineacionVisitante") val alineacionVisitante: List<String> = emptyList()
)

// Petición

data class GuardarJugadorRequest(val nombre: String, val dorsal: Int, val posicion: String, val equipo: String)

data class GuardarInhabilitacionRequest(
    val jugador: String,
    val motivo: String,
    val descripcion: String?,
    val fechaFin: String?
)

data class GuardarEquipoRequest(val nombre: String, val capitan: String?, val contacto: String?)

data class GuardarPartidoRequest(
    val equipoLocal: String,
    val equipoVisitante: String,
    val fechaPartido: String,
    val lugar: String
)

data class GuardarAlineacionRequest(val tipoEquipo: String, val jugadoresIds: List<String>)

// Respuestas

data class EquipoGuardadoResponse(@SerializedName("equipo") val equipo: EquipoApi? = null)

data class PartidoCrudoApi(
    @SerializedName("_id") val id: String = "",
    @SerializedName("alineacionLocal") val alineacionLocal: List<String> = emptyList(),
    @SerializedName("alineacionVisitante") val alineacionVisitante: List<String> = emptyList()
)

data class PartidoResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("partido") val partido: PartidoCrudoApi? = null
)

data class ErrorApi(
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null
)

// Textos

fun InhabilitacionApi.motivoLegible(): String =
    motivo.replace('_', ' ').replaceFirstChar { it.uppercase() }

fun InhabilitacionApi.periodo(): String =
    "${fechaInicio?.take(10) ?: "?"} → ${fechaFin?.take(10) ?: "sin fecha fin"}"

fun InhabilitacionApi.estadoTexto(hoy: String): String = when {
    resuelta -> "Resuelta"
    fechaFin != null && fechaFin.take(10) < hoy -> "Vencida (falta habilitar al jugador)"
    else -> "Activa"
}
