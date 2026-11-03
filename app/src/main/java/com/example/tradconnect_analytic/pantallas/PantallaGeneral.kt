package com.example.tradconnect_analytic.pantallas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradconnect_analytic.datos.DatosPeriodoVentas
import com.example.tradconnect_analytic.datos.PuntoVenta
import kotlin.math.abs

@Composable
fun PantallaGeneral(
    totalVentasHistoricoCount: Int,
    pedidosActivos: Int,
    clientesActivos: Int,
    datosSemana: DatosPeriodoVentas,
    datosMes: DatosPeriodoVentas,
    datosAnio: DatosPeriodoVentas
) {
    // Estado para controlar el filtro temporal seleccionado (Semana, Mes, Año)
    var filtroTemporal by remember { mutableStateOf("Mes") }

    // Seleccionar dinámicamente los datos según el botón presionado
    val datosActuales = when (filtroTemporal) {
        "Semana" -> datosSemana
        "Mes" -> datosMes
        else -> datosAnio
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Total histórico de ventas (Texto superior menos llamativo)
        item {
            Text(
                text = "Total Histórico de Ventas: $totalVentasHistoricoCount transacciones",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 2. Botones de Selector de Período (Semana, Mes, Año)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Semana", "Mes", "Año").forEach { periodo ->
                    Button(
                        onClick = { filtroTemporal = periodo },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (filtroTemporal == periodo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = periodo,
                            color = if (filtroTemporal == periodo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Tarjeta KPI Principal: Cantidad de ventas por período (Números grandes)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ventas por $filtroTemporal",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${datosActuales.ventasTotalPeriodo}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // 4. Gráfico de Tendencia Interactivo con diseño armónico
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Fluctuación (${datosActuales.titulo})",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${datosActuales.ventasTotalPeriodo}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Círculo de check verde
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFF2E7D32), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Estado comparativo
                    Text(
                        text = datosActuales.estadoComparativo,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gráfico Interactivo con Canvas y Detección de Toques
                    GraficoLineaInteractivoCanvas(puntos = datosActuales.puntosHistorial)
                }
            }
        }

        // 5. Indicadores Secundarios: Pedidos Activos y Clientes Activos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Pedidos Activos", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$pedidosActivos",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Clientes Activos", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$clientesActivos",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GraficoLineaInteractivoCanvas(puntos: List<PuntoVenta>) {
    if (puntos.isEmpty()) return

    // Estado para guardar el índice del punto que el usuario toque en la gráfica
    var puntoSeleccionadoIndex by remember(puntos) { mutableStateOf<Int?>(null) }
    val maxValor = (puntos.maxOfOrNull { it.cantidad } ?: 100).coerceAtLeast(1)

    // Obtenemos el color primario actual del tema para la línea del gráfico
    val colorPrimario = MaterialTheme.colorScheme.primary

    Column {
        // Cuadro de información flotante que muestra la fecha y cantidad al tocar un punto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (puntoSeleccionadoIndex != null && puntoSeleccionadoIndex!! < puntos.size) {
                val p = puntos[puntoSeleccionadoIndex!!]
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${p.etiquetaFecha}  | Ventas: ${p.cantidad}",
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Text(
                    text = "Toca un punto de la línea para ver detalles",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Zona del Canvas con detector de gestos táctiles (Tap)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .pointerInput(puntos) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val pasoX = if (puntos.size > 1) width / (puntos.size - 1) else width

                        // Buscar el punto más cercano en el eje X al lugar donde el usuario tocó
                        var indiceMasCercano = 0
                        var menorDistancia = Float.MAX_VALUE

                        puntos.forEachIndexed { index, _ ->
                            val x = index * pasoX
                            val distancia = abs(x - offset.x)
                            if (distancia < menorDistancia) {
                                menorDistancia = distancia
                                indiceMasCercano = index
                            }
                        }

                        // Si el toque está dentro de una tolerancia razonable, seleccionamos el punto
                        if (menorDistancia < 70f) {
                            puntoSeleccionadoIndex = indiceMasCercano
                        } else {
                            puntoSeleccionadoIndex = null
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // 1. Líneas de guía horizontales de fondo adaptadas al tema
                val lineasGuia = 4
                for (i in 0 until lineasGuia) {
                    val y = height * (i.toFloat() / (lineasGuia - 1))
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                // 2. Trazado de la línea de tendencia con el color primario
                val path = Path()
                val pasoX = if (puntos.size > 1) width / (puntos.size - 1) else width

                puntos.forEachIndexed { index, punto ->
                    val x = index * pasoX
                    val y = height - (height * (punto.cantidad.toFloat() / maxValor))

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = colorPrimario,
                    style = Stroke(width = 3.dp.toPx())
                )

                // 3. Dibujar un indicador visual (círculo) si el usuario toca un punto específico
                puntoSeleccionadoIndex?.let { index ->
                    if (index < puntos.size) {
                        val x = index * pasoX
                        val y = height - (height * (puntos[index].cantidad.toFloat() / maxValor))

                        // Círculo exterior de selección
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        // Círculo interior con color primario
                        drawCircle(
                            color = colorPrimario,
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fechas de inicio y fin en la parte inferior del gráfico
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = puntos.first().etiquetaFecha, color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
            Text(text = puntos.last().etiquetaFecha, color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
        }
    }
}