package com.example.tradconnect_analytic.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tradconnect_analytic.datos.ComentarioPostCompra
import com.example.tradconnect_analytic.datos.Incidencia
import com.example.tradconnect_analytic.datos.NecesidadNoSatisfecha

@Composable
fun PantallaIncidenciasYComentarios(
    totalIncidencias: Int,
    listaIncidencias: List<Incidencia>,
    listaComentarios: List<ComentarioPostCompra>,
    listaNecesidades: List<NecesidadNoSatisfecha>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sección 1: Incidencias por proveedor
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Indicador de Incidencias", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Total reportadas: $totalIncidencias", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(text = "Listado de Incidencias", style = MaterialTheme.typography.titleMedium)
        }

        items(listaIncidencias) { incidencia ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = incidencia.titulo, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = incidencia.descripcion)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Estado: ${incidencia.estado}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Sección 2: Comentarios Post-Compra
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Comentarios Post-Compra", style = MaterialTheme.typography.titleLarge)
        }

        items(listaComentarios) { comentario ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = comentario.nombreUsuario, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = comentario.contenido)
                }
            }
        }

        // Sección 3: Necesidades No Satisfechas
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Necesidades No Satisfechas", style = MaterialTheme.typography.titleLarge)
        }

        items(listaNecesidades) { necesidad ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = necesidad.tituloNecesidad, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = necesidad.detalleReporte)
                }
            }
        }
    }
}