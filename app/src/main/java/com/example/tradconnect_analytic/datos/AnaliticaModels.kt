package com.example.tradconnect_analytic.datos

data class PuntoVenta(
    val etiqueta: String,
    val valor: Int
)

data class DatosPeriodoVentas(
    val titulo: String,
    val ventasTotalPeriodo: Int,
    val estadoComparativo: String,
    val puntosHistorial: List<PuntoVenta>
)

data class EvaluacionCliente(
    val id: String,
    val clienteNombre: String,
    val puntuacion: Double,
    val comentario: String?,
    val fecha: String,
    val calidad: Int? = null,
    val tiempoEntrega: Int? = null,
    val precio: Int? = null,
    val atencion: Int? = null
)

data class Incidencia(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val estado: String,
    val fecha: String = "",
    val fechaRespuesta: String = ""
)

data class ComentarioPostCompra(
    val id: String,
    val autor: String,
    val opinion: String,
    val calificacion: Int? = null,
    val fecha: String = "",
    val fechaRespuesta: String = ""
)

data class NecesidadNoSatisfecha(
    val titulo: String,
    val detalle: String
)
