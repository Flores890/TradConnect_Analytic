package com.example.tradconnect_analytic.pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tradconnect_analytic.datos.Incidencia
import java.util.Calendar
import java.util.Locale

@Composable
fun PantallaIncidencias(listaIncidencias: List<Incidencia>, mensajeCarga: String = "") {
    if (listaIncidencias.isEmpty() && mensajeCarga.isBlank()) {
        LazyColumn(Modifier.fillMaxSize().padding(20.dp)) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✓", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Todo en orden", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("No hay incidencias para mostrar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        return
    }

    val todasOrdenadas = listaIncidencias.sortedWith(
        compareByDescending<Incidencia> { normalizarDiaIncidencia(it.fecha) ?: "" }.thenByDescending { it.id }
    )
    val total = listaIncidencias.size
    val resueltas = listaIncidencias.count { it.estaResuelta() }
    val noResueltas = total - resueltas
    val porcentajeResuelto = if (total > 0) resueltas * 100.0 / total else 0.0
    val pendientesTodas = todasOrdenadas.filterNot { it.estaResuelta() }
    val resueltasTodas = todasOrdenadas.filter { it.estaResuelta() }
    val porTipo = listaIncidencias.groupingBy { it.titulo.ifBlank { "Sin tipo" } }.eachCount().toList().sortedByDescending { it.second }
    val porEstado = listaIncidencias.groupingBy { it.estado.ifBlank { "Sin estado" } }.eachCount().toList().sortedByDescending { it.second }
    var vistaSeleccionada by remember { mutableStateOf("Pendientes") }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (mensajeCarga.isNotBlank()) item {
            Text("No se pudieron cargar todas las incidencias: $mensajeCarga", color = MaterialTheme.colorScheme.error)
        }
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Histórico completo", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$total reportes", fontWeight = FontWeight.Bold)
                        Text("$resueltas resueltas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$noResueltas pendientes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (total > 0) {
                        Spacer(Modifier.height(6.dp))
                        Text("${String.format(Locale.getDefault(), "%.0f", porcentajeResuelto)}% del histórico resuelto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Pendientes", "Resueltas", "Resumen").forEach { opcion ->
                    val cuenta = when (opcion) {
                        "Pendientes" -> pendientesTodas.size
                        "Resueltas" -> resueltasTodas.size
                        else -> total
                    }
                    Card(
                        modifier = Modifier.weight(1f).clickable(role = Role.Button) { vistaSeleccionada = opcion },
                        colors = CardDefaults.cardColors(
                            containerColor = if (vistaSeleccionada == opcion) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp)) {
                            Text(opcion, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(cuenta.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Ver ›", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        when (vistaSeleccionada) {
            "Pendientes" -> {
                if (pendientesTodas.isEmpty()) item { TextoVacioIncidencias("No hay reportes abiertos.") }
                items(pendientesTodas, key = { "pendiente-${it.id}" }) { incidencia ->
                    TarjetaIncidencia(incidencia, normalizarDiaIncidencia(incidencia.fecha), pendiente = true)
                }
            }
            "Resueltas" -> {
                if (resueltasTodas.isEmpty()) item { TextoVacioIncidencias("No hay incidencias resueltas.") }
                items(resueltasTodas, key = { "resuelta-${it.id}" }) { incidencia ->
                    TarjetaIncidencia(incidencia, normalizarDiaIncidencia(incidencia.fecha), pendiente = false)
                }
            }
            else -> {
                if (porTipo.isNotEmpty()) {
                    item { TituloIncidencias("Por tipo") }
                    items(porTipo.chunked(2)) { pareja ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pareja.forEach { (tipo, cantidad) ->
                                TarjetaConteoIncidencias(tipo, cantidad, total, Modifier.weight(1f))
                            }
                            if (pareja.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                if (porEstado.isNotEmpty()) {
                    item { TituloIncidencias("Por estado") }
                    items(porEstado.chunked(2)) { pareja ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pareja.forEach { (estado, cantidad) ->
                                TarjetaConteoIncidencias(estado, cantidad, total, Modifier.weight(1f))
                            }
                            if (pareja.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                if (porTipo.isEmpty() && porEstado.isEmpty()) item { TextoVacioIncidencias("Aún no hay datos para resumir.") }
            }
        }
    }
}

@Composable
private fun TextoVacioIncidencias(texto: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(texto, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TarjetaIncidencia(incidencia: Incidencia, fecha: String?, pendiente: Boolean) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(14.dp)) {
            Text(incidencia.titulo.ifBlank { "Incidencia" }, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(incidencia.descripcion.ifBlank { "El reporte no incluye un reclamo detallado." })
            Spacer(Modifier.height(6.dp))
            Text("Estado: ${incidencia.estado.ifBlank { "Sin estado" }}", color = MaterialTheme.colorScheme.secondary)
            val dias = fecha?.let(::diasDesde)
            val detalleFecha = when {
                fecha == null -> "Fecha no disponible"
                pendiente && dias != null -> "$fecha · ${dias} días sin resolver"
                !pendiente -> fecha
                else -> "$fecha · fecha posterior a hoy"
            }
            Text(detalleFecha, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TituloIncidencias(titulo: String, subtitulo: String? = null) {
    Column {
        Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (subtitulo != null) Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TarjetaConteoIncidencias(titulo: String, cantidad: Int, total: Int, modifier: Modifier = Modifier) {
    val porcentaje = if (total > 0) cantidad * 100.0 / total else 0.0
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            Text(cantidad.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${String.format(Locale.getDefault(), "%.0f", porcentaje)}% del total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun Incidencia.estaResuelta(): Boolean = estado.trim().contains("resuelt", ignoreCase = true)

private fun normalizarDiaIncidencia(fecha: String): String? {
    val dia = fecha.trim().substringBefore("T").substringBefore(" ").take(10)
    return try {
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        formato.parse(dia)
        dia
    } catch (_: Exception) { null }
}

private fun diasDesde(fecha: String): Long? {
    val registrada = try { java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(fecha) } catch (_: Exception) { null } ?: return null
    val diaRegistrado = Calendar.getInstance().apply {
        time = registrada
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val hoy = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val diferencia = hoy.timeInMillis - diaRegistrado.timeInMillis
    if (diferencia < -43_200_000L) return null
    return ((diferencia + 43_200_000L) / 86_400_000L).coerceAtLeast(0L)
}
