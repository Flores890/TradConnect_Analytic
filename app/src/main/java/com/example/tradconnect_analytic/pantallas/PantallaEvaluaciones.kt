package com.example.tradconnect_analytic.pantallas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tradconnect_analytic.datos.EvaluacionCliente
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PantallaEvaluaciones(
    promedioCalificacion: Double,
    listaEvaluaciones: List<EvaluacionCliente>,
    mensajeCarga: String = ""
) {
    val aspectos = listOf(
        "Calidad" to listaEvaluaciones.mapNotNull { it.calidad },
        "Tiempo de entrega" to listaEvaluaciones.mapNotNull { it.tiempoEntrega },
        "Precio" to listaEvaluaciones.mapNotNull { it.precio },
        "Atención" to listaEvaluaciones.mapNotNull { it.atencion }
    ).mapNotNull { (nombre, valores) ->
        if (valores.isEmpty()) null else nombre to valores.average()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (mensajeCarga.isNotBlank()) item {
            Text("No se pudieron cargar todas las evaluaciones: $mensajeCarga", color = MaterialTheme.colorScheme.error)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Calificación promedio", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    if (listaEvaluaciones.isEmpty()) {
                        Text(
                            "Sin evaluaciones",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            String.format(Locale.getDefault(), "%.1f", promedioCalificacion),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        EstrellasProporcionales(promedioCalificacion)
                        Text(
                            listaEvaluaciones.size.toString() + " evaluaciones recibidas",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Promedio por aspecto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (aspectos.isEmpty()) {
            item { Text("La API no devolvió puntuaciones por aspecto.") }
        } else {
            items(aspectos) { (nombre, promedio) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(nombre)
                            Text(
                                String.format(Locale.getDefault(), "%.1f / 5", promedio),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        BarraProporcional((promedio / 5.0).toFloat())
                    }
                }
            }
        }

        item {
            Text(
                "Opiniones de clientes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (listaEvaluaciones.isEmpty()) {
            item { Text("Todavía no hay evaluaciones para esta empresa.") }
        } else {
            items(listaEvaluaciones) { evaluacion ->
                var mostrarAspectos by remember(evaluacion.id) { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                evaluacion.clienteNombre.ifBlank { "Cliente" },
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                String.format(Locale.getDefault(), "%.1f / 5", evaluacion.puntuacion),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        EstrellasProporcionales(evaluacion.puntuacion)
                        Spacer(Modifier.height(6.dp))
                        Text(evaluacion.comentario ?: "Sin comentario")
                        Spacer(Modifier.height(6.dp))
                        Text(
                            evaluacion.fecha.substringBefore("T").substringBefore(" ").ifBlank { "Fecha no disponible" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { mostrarAspectos = !mostrarAspectos }) {
                            Text(if (mostrarAspectos) "Ocultar puntuaciones" else "Ver puntuaciones por aspecto")
                        }
                        if (mostrarAspectos) {
                            val valores = listOf(
                                "Calidad" to evaluacion.calidad,
                                "Tiempo de entrega" to evaluacion.tiempoEntrega,
                                "Precio" to evaluacion.precio,
                                "Atención" to evaluacion.atencion
                            ).filter { it.second != null }
                            if (valores.isEmpty()) {
                                Text("Esta evaluación no incluye puntuaciones por aspecto.")
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    valores.forEach { (nombre, valor) ->
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(nombre)
                                            Text("${valor} / 5", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstrellasProporcionales(calificacion: Double) {
    val puntuacion = calificacion.coerceIn(0.0, 5.0)
    val colorRelleno = MaterialTheme.colorScheme.primary
    val colorVacio = MaterialTheme.colorScheme.outline

    Row {
        repeat(5) { indice ->
            val fraccion = (puntuacion - indice).coerceIn(0.0, 1.0).toFloat()
            Canvas(Modifier.size(28.dp)) {
                val centro = Offset(size.width / 2f, size.height / 2f)
                val radioExterior = size.minDimension * 0.46f
                val radioInterior = radioExterior * 0.44f
                val path = Path()

                for (punto in 0 until 10) {
                    val radio = if (punto % 2 == 0) radioExterior else radioInterior
                    val angulo = -PI / 2.0 + punto * PI / 5.0
                    val x = centro.x + cos(angulo).toFloat() * radio
                    val y = centro.y + sin(angulo).toFloat() * radio
                    if (punto == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()

                drawPath(
                    path = path,
                    color = colorVacio,
                    style = Stroke(width = 1.4.dp.toPx())
                )
                clipRect(right = size.width * fraccion) {
                    drawPath(path = path, color = colorRelleno)
                }
            }
        }
    }
}

@Composable
private fun BarraProporcional(progreso: Float) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(9.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progreso.coerceIn(0f, 1f))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}


