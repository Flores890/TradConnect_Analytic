package com.example.tradconnect_analytic.datos

// Representa un registro de venta comparativo
data class VentaPeriodo(
    val id: String,
    val etiquetaPeriodo: String, // Ej: "Sem 1", "Ene", "2024"
    val ventasActuales: Int,
    val ventasAnterior: Int
)

// Datos generales del proveedor y KPIs
data class DatosProveedor(
    val totalVentasHistorico: Double,
    val pedidosActivos: Int,
    val clientesActivos: Int,
    val calificacionPromedio: Double,
    val totalIncidencias: Int
)

data class EvaluacionCliente(
    val id: String,
    val nombreCliente: String,
    val puntuacion: Double,
    val comentario: String,
    val fecha: String
)

data class Incidencia(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val estado: String
)

data class ComentarioPostCompra(
    val id: String,
    val nombreUsuario: String,
    val contenido: String
)

data class NecesidadNoSatisfecha(
    val id: String,
    val tituloNecesidad: String,
    val detalleReporte: String
)