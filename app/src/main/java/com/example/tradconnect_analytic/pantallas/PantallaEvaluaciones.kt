package com.example.tradconnect_analytic.pantallas


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradconnect_analytic.datos.EvaluacionCliente

@Composable
fun PantallaEvaluaciones(
    promedioCalificacion: Double,
    listaEvaluaciones: List<EvaluacionCliente>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Tarjeta con Calificación Promedio y Barra de Estrellas que se carga
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Calificación Promedio",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Valor numérico grande
                    Text(
                        text = String.format("%.1f", promedioCalificacion),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Barra de estrellas que se carga proporcionalmente al promedio
                    BarraEstrellasQueSeCarga(
                        calificacion = promedioCalificacion
                    )
                }
            }
        }

        // 2. Título de la sección de Evaluaciones
        item {
            Text(
                text = "Evaluaciones de Clientes (${listaEvaluaciones.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. Listado completo de todas las evaluaciones recibidas
        items(listaEvaluaciones) { evaluacion ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = evaluacion.nombreCliente,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Calificación individual del cliente en estrellas
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${evaluacion.puntuacion} ",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(text = "★", color = Color(0xFFFFB300))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = evaluacion.comentario,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = evaluacion.fecha,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Barra compuesta de estrellas que se carga dinámicamente según la calificación (ej. 4.6 / 5)
 */
@Composable
fun BarraEstrellasQueSeCarga(
    calificacion: Double
) {
    // Calculamos el porcentaje de llenado (ej: 4.6 / 5.0 = 0.92 -> 92%)
    val porcentaje = (calificacion / 5.0).coerceIn(0.0, 1.0).toFloat()

    val colorDorado = Color(0xFFFFB300)
    val colorGrisFondo = Color.Gray.copy(alpha = 0.3f)

    Box(
        contentAlignment = Alignment.CenterStart
    ) {
        // Capa base: 5 estrellas vacías (grises) de fondo
        Text(
            text = "★★★★★",
            fontSize = 32.sp,
            color = colorGrisFondo,
            letterSpacing = 2.sp
        )

        // Capa superior: 5 estrellas doradas recortadas exactamente al porcentaje de la calificación
        Row(
            modifier = Modifier.fillMaxWidth(porcentaje),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "★★★★★",
                fontSize = 32.sp,
                color = colorDorado,
                letterSpacing = 2.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}