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
    var filtroTemporal by remember { mutableStateOf("Mes") }

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
        item {
            Text(
                text = "Total Histórico de Ventas: $totalVentasHistoricoCount transacciones",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFF2E7D32), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = datosActuales.estadoComparativo,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GraficoLineaInteractivoCanvas(puntos = datosActuales.puntosHistorial)
                }
            }
        }

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

    var puntoSeleccionadoIndex by remember(puntos) { mutableStateOf<Int?>(null) }
    val maxValor = (puntos.maxOfOrNull { it.valor } ?: 100).coerceAtLeast(1)
    val colorPrimario = MaterialTheme.colorScheme.primary

    Column {
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
                        text = "${p.etiqueta}  | Ventas: ${p.valor}",
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .pointerInput(puntos) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val pasoX = if (puntos.size > 1) width / (puntos.size - 1) else width
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

                val path = Path()
                val pasoX = if (puntos.size > 1) width / (puntos.size - 1) else width

                puntos.forEachIndexed { index, punto ->
                    val x = index * pasoX
                    val y = height - (height * (punto.valor.toFloat() / maxValor))

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

                puntoSeleccionadoIndex?.let { index ->
                    if (index < puntos.size) {
                        val x = index * pasoX
                        val y = height - (height * (puntos[index].valor.toFloat() / maxValor))

                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = puntos.first().etiqueta, color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
            Text(text = puntos.last().etiqueta, color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
        }
    }
}