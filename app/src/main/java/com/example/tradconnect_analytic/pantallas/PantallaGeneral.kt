package com.example.tradconnect_analytic.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tradconnect_analytic.datos.VentaPeriodo

@Composable
fun PantallaGeneral(
    totalHistorico: Double,
    pedidosActivos: Int,
    clientesActivos: Int,
    listaVentas: List<VentaPeriodo>
) {
    var filtroTemporal by remember { mutableStateOf("Mes") } // "Semana", "Mes", "Año"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Total histórico de ventas (menos llamativo en la parte superior)
        Text(
            text = "Ventas Totales Históricas: $${String.format("%.2f", totalHistorico)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Selector de período: Semana, Mes, Año
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

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta KPI Principal: Cantidad de ventas por período (Números grandes)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ventas por $filtroTemporal",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Número grande visible
                Text(
                    text = if (filtroTemporal == "Semana") "142" else if (filtroTemporal == "Mes") "580" else "6,420",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Indicadores secundarios lado a lado: Pedidos Activos y Clientes Activos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pedidos Activos", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$pedidosActivos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Clientes Activos", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$clientesActivos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Comparativa de Ventas (Período Actual vs Anterior)",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Diagrama de barras deslizable (Simulado con LazyColumn de hasta 20 registros)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaVentas) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Registro: ${item.etiquetaPeriodo}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Actual: ${item.ventasActuales} ventas")
                            Text("Anterior: ${item.ventasAnterior} ventas")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Barra gráfica proporcional simulada
                        LinearProgressIndicator(
                            progress = { (item.ventasActuales.toFloat() / 1000f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        )
                    }
                }
            }
        }
    }
}