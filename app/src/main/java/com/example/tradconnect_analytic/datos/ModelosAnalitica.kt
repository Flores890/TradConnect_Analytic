package com.example.tradconnect_analytic.datos
// Punto individual para el gráfico de línea / tendencia histórica
data class PuntoVenta(
    val etiquetaFecha: String, // Ej: "25 sept", "22 oct"
    val cantidad: Int
)

// Estructura para la tarjeta comparativa con su historial de puntos
data class DatosPeriodoVentas(
    val titulo: String,            // Ej: "Ventas por Semana", "Ventas por Mes"
    val ventasTotalPeriodo: Int,  // Valor en grande (ej: 460)
    val estadoComparativo: String, // Ej: "Casi igual a lo habitual", "+15% vs anterior"
    val puntosHistorial: List<PuntoVenta> // Registros para dibujar el gráfico (10 a 20 puntos)
)

// Datos generales del proveedor y KPIs
data class DatosProveedor(
    val totalVentasHistoricoCount: Int,
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