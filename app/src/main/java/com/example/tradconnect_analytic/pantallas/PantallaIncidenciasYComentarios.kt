package com.example.tradconnect_analytic.pantallas

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tradconnect_analytic.datos.Incidencia
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class IncidenciaConFecha(val incidencia: Incidencia, val fecha: String)

@Composable
fun PantallaIncidencias(listaIncidencias: List<Incidencia>, mensajeCarga: String = "") {
    val hoy = Calendar.getInstance()
    val inicioVentana = (hoy.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -89)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val fechaInicio = formatoIncidencia.format(inicioVentana.time)
    val fechaFin = formatoIncidencia.format(hoy.time)
    val conFecha = listaIncidencias.mapNotNull { incidencia ->
        val fecha = normalizarDiaIncidencia(incidencia.fecha) ?: return@mapNotNull null
        IncidenciaConFecha(incidencia, fecha)
    }
    val recientes = conFecha.filter { it.fecha >= fechaInicio && it.fecha <= fechaFin }.sortedByDescending { it.fecha }
    val sinFecha = listaIncidencias.size - conFecha.size
    val fueraVentana = conFecha.count { it.fecha < fechaInicio }
    val posteriores = conFecha.count { it.fecha > fechaFin }
    val total = recientes.size
    val resueltas = recientes.count { it.incidencia.estaResuelta() }
    val noResueltas = total - resueltas
    val pendientes = recientes.count { it.incidencia.estado.trim().equals("Pendiente", ignoreCase = true) }
    val porcentajeResuelto = if (total > 0) resueltas * 100.0 / total else 0.0
    val pendientesTodas = listaIncidencias.filterNot { it.estaResuelta() }
        .sortedWith(compareBy<Incidencia> { normalizarDiaIncidencia(it.fecha) ?: "9999-12-31" }.thenBy { it.id })
    val pendientesAntiguas = pendientesTodas.count { normalizarDiaIncidencia(it.fecha)?.let { dia -> dia < fechaInicio } == true }
    val pendientesSinFecha = pendientesTodas.count { normalizarDiaIncidencia(it.fecha) == null }
    val resueltasRecientes = recientes.filter { it.incidencia.estaResuelta() }
    val porTipo = recientes.groupingBy { it.incidencia.titulo.ifBlank { "Sin tipo" } }.eachCount().toList().sortedByDescending { it.second }
    val porEstado = recientes.groupingBy { it.incidencia.estado.ifBlank { "Sin estado" } }.eachCount().toList().sortedByDescending { it.second }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (mensajeCarga.isNotBlank()) item {
            Text("No se pudieron cargar todas las incidencias: $mensajeCarga", color = MaterialTheme.colorScheme.error)
        }
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Desempeño de incidencias · últimos 90 días", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(total.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("$resueltas resueltas · $noResueltas sin resolver · $pendientes pendientes")
                    Spacer(Modifier.height(10.dp))
                    Text(if (total == 0) "Sin incidencias fechadas en este periodo." else "Resueltas: ${String.format(Locale.getDefault(), "%.0f", porcentajeResuelto)}% de los reportes del periodo.")
                    Spacer(Modifier.height(8.dp))
                    BarraIncidencias(if (total == 0) 0f else resueltas.toFloat() / total)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Registros recibidos: ${listaIncidencias.size}; dentro de 90 días: $total; anteriores: $fueraVentana; sin fecha válida: $sinFecha; posteriores a hoy: $posteriores.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("La API no aporta fechas de respuesta suficientes para medir rapidez.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Pendientes por atender · todas las fechas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(pendientesTodas.size.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Incluye reportes antiguos y sin fecha; no se ocultan por antigüedad.")
                    if (pendientesAntiguas > 0 || pendientesSinFecha > 0) {
                        Text("Anteriores a 90 días: $pendientesAntiguas · sin fecha válida: $pendientesSinFecha", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item { TituloIncidencias("Incidencias por tipo · últimos 90 días") }
        if (porTipo.isEmpty()) item { Text("No hay incidencias fechadas en los últimos 90 días.") }
        else items(porTipo) { (tipo, cantidad) -> FilaConteoIncidencias(tipo, cantidad, total) }
        item { TituloIncidencias("Incidencias por estado · últimos 90 días") }
        if (porEstado.isEmpty()) item { Text("No hay estados recientes para mostrar.") }
        else items(porEstado) { (estado, cantidad) -> FilaConteoIncidencias(estado, cantidad, total) }
        item { TituloIncidencias("Pendientes por atender", "Ordenadas desde la más antigua; incluye cualquier fecha") }
        if (pendientesTodas.isEmpty()) item { Text("No hay incidencias pendientes o en proceso.") }
        else items(pendientesTodas, key = { "pendiente-${it.id}" }) { incidencia ->
            TarjetaIncidencia(
                incidencia = incidencia,
                fecha = normalizarDiaIncidencia(incidencia.fecha),
                pendiente = true
            )
        }
        item { TituloIncidencias("Incidencias resueltas recientes", "Últimos 90 días; las más antiguas no se cuentan en la tendencia") }
        if (resueltasRecientes.isEmpty()) item { Text("No hay incidencias resueltas con fecha válida en los últimos 90 días.") }
        else items(resueltasRecientes, key = { "resuelta-${it.incidencia.id}" }) { fila ->
            val incidencia = fila.incidencia
            TarjetaIncidencia(incidencia = incidencia, fecha = fila.fecha, pendiente = false)
        }
        item {
            Text("Las pendientes se muestran sin límite de antigüedad. Las resueltas anteriores a 90 días quedan fuera de esta vista resumida.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
            Text("Estado: ${incidencia.estado.ifBlank { "Sin estado" }}", color = MaterialTheme.colorScheme.primary)
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
private fun FilaConteoIncidencias(titulo: String, cantidad: Int, maximo: Int) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(titulo); Text(cantidad.toString(), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            BarraIncidencias(cantidad.toFloat() / maximo.coerceAtLeast(1))
        }
    }
}

@Composable
private fun BarraIncidencias(progreso: Float) {
    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(progreso.coerceIn(0f, 1f)).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
    }
}

private val formatoIncidencia = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
private fun Incidencia.estaResuelta(): Boolean = estado.trim().contains("resuelt", ignoreCase = true)

private fun normalizarDiaIncidencia(fecha: String): String? {
    val dia = fecha.trim().substringBefore("T").substringBefore(" ").take(10)
    return try { formatoIncidencia.parse(dia); dia } catch (_: Exception) { null }
}

private fun diasDesde(fecha: String): Long? {
    val registrada = try { formatoIncidencia.parse(fecha) } catch (_: Exception) { null } ?: return null
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
