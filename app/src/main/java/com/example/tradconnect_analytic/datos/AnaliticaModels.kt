package com.example.tradconnect_analytic.datos

// Estructura para los puntos de los gráficos de ventas
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

// Estructura para las evaluaciones de proveedores (MongoDB / SQL)
data class EvaluacionCliente(
    val id: String,
    val clienteNombre: String,
    val puntuacion: Double,
    val comentario: String?,
    val fecha: String
)

// Estructura para incidencias
data class Incidencia(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val estado: String
)

// Estructura para comentarios post-compra
data class ComentarioPostCompra(
    val id: String,
    val autor: String,
    val opinion: String
)

// Estructura para necesidades no satisfechas
data class NecesidadNoSatisfecha(
    val id: String,
    val titulo: String,
    val detalle: String
)