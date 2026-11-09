package com.example.tradconnect_analytic.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tradconnect_analytic.datos.AnaliticaProveedorApi
import com.example.tradconnect_analytic.datos.CategoriaVentaApi
import com.example.tradconnect_analytic.datos.MetodoPagoApi
import com.example.tradconnect_analytic.datos.PedidoSqlApi
import com.example.tradconnect_analytic.datos.ProductoCatalogoApi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class PeriodoVentas(val titulo: String) {
    MES("Este mes"),
    TRES_MESES("3 meses"),
    ANIO("Este año"),
    HISTORICO("Histórico")
}

private data class PedidoMedible(
    val pedido: PedidoSqlApi,
    val fecha: String,
    val total: Double
)

private data class ProductoResumen(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val unidades: Double,
    val ingreso: Double
)

private data class CategoriaResumen(
    val nombre: String,
    val unidades: Double,
    val ingreso: Double
)

private data class ClienteResumen(
    val id: String,
    val nombre: String,
    val pedidos: Int,
    val ultimaCompra: String,
    val productoMasSolicitado: String?
)

private val formatoDiaIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)

@Composable
fun PantallaVentasApi(
    datos: AnaliticaProveedorApi,
    pedidos: List<PedidoSqlApi>,
    productosCatalogo: List<ProductoCatalogoApi>,
    metodosPago: List<MetodoPagoApi>,
    empresaId: String?,
    pedidosSqlDisponibles: Boolean,
    productosSqlDisponibles: Boolean,
    mensajePedidos: String,
    mensajeProductos: String
) {
    var periodo by remember { mutableStateOf(PeriodoVentas.HISTORICO) }
    val formato = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val hoy = Calendar.getInstance()
    val hoyIso = formatoDiaIso.format(hoy.time)
    val mesActual = hoyIso.take(7)
    val mesAnterior = SimpleDateFormat("yyyy-MM", Locale.US).format(
        (hoy.clone() as Calendar).apply { add(Calendar.MONTH, -1) }.time
    )
    val inicioPeriodo = inicioPeriodo(periodo, hoy)
    val inicioIso = formatoDiaIso.format(inicioPeriodo.time)
    val inicioMesPeriodo = inicioIso.take(7)

    // El resumen usa todos los estados de pedido salvo Cancelado/Cancelada.
    val idEmpresa = empresaId?.takeIf { it.isNotBlank() }
    val pedidosPropios = if (idEmpresa == null) emptyList() else pedidos.filter {
        val estado = it.estadoActual.orEmpty().trim()
        it.proveedorId == idEmpresa &&
            !estado.equals("Cancelado", ignoreCase = true) &&
            !estado.equals("Cancelada", ignoreCase = true)
    }
    val pedidosContabilizados = if (idEmpresa == null || datos.monedaId == null || !pedidosSqlDisponibles) {
        emptyList()
    } else {
        pedidosPropios.filter { it.monedaId == datos.monedaId }
    }
    val pedidosContabilizadosConFecha = pedidosContabilizados.mapNotNull { pedido ->
        val fecha = normalizarFecha(pedido.fechaPedido) ?: return@mapNotNull null
        val total = pedido.totalNeto?.toDoubleOrNull() ?: return@mapNotNull null
        if (fecha > hoyIso) return@mapNotNull null
        PedidoMedible(pedido, fecha, total)
    }
    val ventasMensuales = pedidosContabilizadosConFecha
        .groupBy { it.fecha.take(7) }
        .map { (mes, filas) -> mes to filas.sumOf { it.total } }
        .sortedBy { it.first }
    val pedidosPorMes = pedidosContabilizadosConFecha.groupingBy { it.fecha.take(7) }.eachCount()
    val ventasMensualesPeriodo = ventasMensuales.filter { it.first >= inicioMesPeriodo && it.first <= mesActual }
    val ventaMesActual = ventasMensuales.firstOrNull { it.first == mesActual }?.second ?: 0.0
    val pedidosMesActual = pedidosPorMes[mesActual] ?: 0
    val diaComparableMesAnterior = minOf(
        hoy.get(Calendar.DAY_OF_MONTH),
        (hoy.clone() as Calendar).apply { add(Calendar.MONTH, -1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
    )
    val ventaMesAnteriorComparable = pedidosContabilizadosConFecha
        .filter { fila ->
            fila.fecha.take(7) == mesAnterior &&
                (fila.fecha.substring(8, 10).toIntOrNull() ?: 0) <= diaComparableMesAnterior
        }
        .sumOf { it.total }
    val variacion = if (ventaMesAnteriorComparable > 0.0) {
        (ventaMesActual - ventaMesAnteriorComparable) / ventaMesAnteriorComparable * 100.0
    } else null
    val ventasPorAnio = ventasMensuales
        .groupBy { it.first.take(4) }
        .mapValues { (_, filas) -> filas.sumOf { it.second } }
        .toSortedMap()
    val pedidosMedibles = pedidosContabilizadosConFecha.filter {
        it.fecha >= inicioIso && it.fecha <= hoyIso
    }
    val ventaPeriodo = pedidosMedibles.sumOf { it.total }
    val numeroPedidosPeriodo = pedidosMedibles.size
    val ticketPromedio = if (numeroPedidosPeriodo > 0) ventaPeriodo / numeroPedidosPeriodo else 0.0
    val catalogoPropio = if (idEmpresa == null) emptyList() else productosCatalogo.filter {
        it.empresaId == idEmpresa
    }
    val mapaProductos = catalogoPropio.mapNotNull { producto ->
        val id = producto.id ?: return@mapNotNull null
        id to producto
    }.toMap()
    val lineas = pedidosMedibles.flatMap { it.pedido.detalles }
        .filter { it.productoId != null }
    val detalleCompleto = pedidosMedibles.isEmpty() || pedidosMedibles.all { it.pedido.detalles.isNotEmpty() }
    val rankingProductos = if (detalleCompleto) {
        lineas.groupBy { it.productoId!! }.map { (id, lineasProducto) ->
            val producto = mapaProductos[id]
            ProductoResumen(
                id = id,
                nombre = producto?.nombre.orEmpty().ifBlank {
                    lineasProducto.firstNotNullOfOrNull { it.productoNombre?.takeIf(String::isNotBlank) } ?: "Producto $id"
                },
                categoria = producto?.categoria.orEmpty().ifBlank { "Categoría no disponible" },
                unidades = lineasProducto.sumOf { it.cantidad?.toDoubleOrNull() ?: 0.0 },
                ingreso = lineasProducto.sumOf { it.subtotalLinea?.toDoubleOrNull() ?: 0.0 }
            )
        }.sortedWith(compareByDescending<ProductoResumen> { it.unidades }.thenByDescending { it.ingreso })
    } else emptyList()
    val productosConVenta = rankingProductos.filter { it.unidades > 0.0 }
    val rankingProductosVendidos = productosConVenta
    val promedioUnidades = productosConVenta.map { it.unidades }.average().takeIf { !it.isNaN() } ?: 0.0
    val productosBajoPromedio = productosConVenta.count { it.unidades < promedioUnidades }

    val categorias = rankingProductos
        .filter { it.unidades > 0.0 && it.categoria != "Categoría no disponible" }
        .groupBy { it.categoria }
        .map { (nombre, productos) ->
            CategoriaResumen(nombre, productos.sumOf { it.unidades }, productos.sumOf { it.ingreso })
        }
        .sortedByDescending { it.ingreso }
    val maxIngresoCategoria = categorias.maxOfOrNull { it.ingreso }?.takeIf { it > 0.0 } ?: 1.0

    val ventasSemana = ventasSemanales(pedidosMedibles, inicioIso, hoyIso)
    val ventasDiaActual = ventasDiariasSemanaActual(pedidosMedibles, hoy)
    val promedioPorDia = promediosPorDiaSemana(pedidosMedibles, inicioPeriodo, hoy)

    val nombreMetodo = metodosPago.mapNotNull { metodo ->
        val id = metodo.id ?: return@mapNotNull null
        id to metodo.nombremetodo.orEmpty().ifBlank { "Método $id" }
    }.toMap()
    val metodosUsados = pedidosMedibles.mapNotNull { fila ->
        fila.pedido.metodoPagoId?.let { id -> nombreMetodo[id] ?: "Método $id" }
    }.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }

    val clientes = pedidosMedibles.groupBy { it.pedido.restauranteId.orEmpty() }
        .filterKeys { it.isNotBlank() }
        .map { (clienteId, filas) ->
            val cantidadesProducto = filas.flatMap { it.pedido.detalles }
                .filter { it.productoId != null }
                .groupBy { it.productoId!! }
                .mapValues { (_, detalles) -> detalles.sumOf { it.cantidad?.toDoubleOrNull() ?: 0.0 } }
            val productoMasSolicitado = cantidadesProducto.maxByOrNull { it.value }?.key
                ?.let { id ->
                    mapaProductos[id]?.nombre ?: filas.flatMap { it.pedido.detalles }
                        .firstOrNull { it.productoId == id }?.productoNombre
                }
            ClienteResumen(
                id = clienteId,
                nombre = filas.firstNotNullOfOrNull { it.pedido.restauranteNombre?.takeIf(String::isNotBlank) }
                    ?: "Cliente",
                pedidos = filas.size,
                ultimaCompra = filas.maxOf { it.fecha },
                productoMasSolicitado = productoMasSolicitado
            )
        }
        .sortedWith(compareByDescending<ClienteResumen> { it.pedidos }.thenByDescending { it.ultimaCompra })
        .take(10)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (!pedidosSqlDisponibles) item {
            Text(mensajePedidos.ifBlank { "La ruta de pedidos no devolvió datos válidos." }, color = MaterialTheme.colorScheme.error)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Ventas registradas de $mesActual", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(formato.format(ventaMesActual), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("$pedidosMesActual pedidos registrados · moneda predeterminada de la API")
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Variación frente a los mismos días del mes anterior", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    if (variacion == null) Text("No hay ventas del mes anterior para calcular el porcentaje.")
                    else {
                        Text(
                            (if (variacion >= 0.0) "+" else "") + formato.format(variacion) + "%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (variacion >= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text("Todos los estados excepto cancelado · comparación al mismo día del mes anterior.")
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Ticket promedio · ${periodo.titulo}")
                    Text(formato.format(ticketPromedio), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("$numeroPedidosPeriodo pedidos; montos de la moneda predeterminada.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text("Periodo para pedidos, productos y clientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodoVentas.values().forEach { opcion ->
                    FilterChip(
                        selected = periodo == opcion,
                        onClick = { periodo = opcion },
                        label = { Text(opcion.titulo) }
                    )
                }
            }
            Text("Empresa vinculada: ${idEmpresa ?: "no disponible"} · desde $inicioIso · datos limitados a esta empresa y moneda.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { Titulo("Ventas por mes") }
        if (ventasMensualesPeriodo.isEmpty()) item {
            val conFechaYTotal = pedidosContabilizados.count {
                normalizarFecha(it.fechaPedido) != null && it.totalNeto?.toDoubleOrNull() != null
            }
            Text("No hay ventas válidas en el periodo seleccionado ($inicioIso a $hoyIso).")
            Text(
                "Diagnóstico de la respuesta: ${pedidosPropios.size} pedidos de esta empresa; " +
                    "${pedidosPropios.size} pedidos incluidos; se excluyen únicamente los cancelados; " +
                    "${pedidosContabilizados.size} en la moneda ${datos.monedaId ?: "no disponible"}; " +
                    "$conFechaYTotal con fecha y total válidos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        else item { GraficoBarras(ventasMensualesPeriodo.takeLast(12).map { it.first }, ventasMensualesPeriodo.takeLast(12).map { it.second }, formato) }

        item {
            Titulo("Comparación anual", "Histórico completo disponible para el proveedor")
        }
        if (ventasPorAnio.isEmpty()) item { Text("No hay años con ventas disponibles.") }
        else item { GraficoBarras(ventasPorAnio.keys.toList(), ventasPorAnio.values.toList(), formato) }

        item { Titulo("Ventas recientes por semana", "Máximo ocho semanas dentro del periodo elegido") }
        if (!pedidosSqlDisponibles || datos.monedaId == null || idEmpresa == null) item { Text(mensajePedidos.ifBlank { "No fue posible validar pedidos de este proveedor." }) }
        else if (ventasSemana.isEmpty()) item { Text("Sin pedidos válidos en el periodo elegido.") }
        else item { GraficoBarras(ventasSemana.map { it.first }, ventasSemana.map { it.second }, formato) }

        item { Titulo("Ventas por día de esta semana") }
        if (ventasDiaActual.isEmpty()) item { Text("No hay pedidos SQL para mostrar esta semana.") }
        else item { GraficoBarras(ventasDiaActual.map { it.first }, ventasDiaActual.map { it.second }, formato) }

        item { Titulo("Promedio por día de la semana", "Incluye días sin ventas desde el inicio del periodo") }
        if (promedioPorDia.isEmpty()) item { Text("No hay pedidos fechados para calcular promedios.") }
        else item { GraficoBarras(promedioPorDia.map { it.first }, promedioPorDia.map { it.second }, formato) }

        item { Titulo("Métodos de pago en pedidos registrados", "Todos los estados excepto cancelado; no representa preferencias declaradas") }
        if (metodosUsados.isEmpty()) item { Text("No hay métodos de pago registrados en los pedidos del periodo.") }
        else items(metodosUsados) { (nombre, cantidad) -> FilaConteo(nombre, cantidad, metodosUsados.maxOf { it.second }, "pedidos") }

        item { Titulo("Categorías compradas", "Calculadas desde las líneas de pedido y el catálogo propio") }
        if (!detalleCompleto) item { Text("No se recibió el detalle completo de los pedidos; se omiten categorías.") }
        else if (!productosSqlDisponibles) item { Text(mensajeProductos.ifBlank { "No se pudo obtener el catálogo propio; las categorías no están disponibles." }) }
        else if (categorias.isEmpty()) item { Text("No hay ventas por categoría en el periodo elegido.") }
        else items(categorias) { categoria -> TarjetaCategoria(categoria, formato, maxIngresoCategoria) }

        item {
            Titulo("Productos en pedidos", "Solo artículos presentes en pedidos reales de esta empresa")
            if (rankingProductosVendidos.isNotEmpty()) Text(
                "$productosBajoPromedio productos vendidos por debajo del promedio de unidades.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!detalleCompleto) {
            item {
                Text("No se recibió el detalle completo de los pedidos; no se muestra un ranking parcial.", color = MaterialTheme.colorScheme.error)
            }
        } else if (rankingProductosVendidos.isEmpty()) item { Text("No hay productos vendidos en este periodo para esta empresa.") }
        else items(rankingProductosVendidos) { producto ->
            val estado = when {
                producto.unidades == 0.0 -> "Sin ventas en el periodo"
                producto.unidades < promedioUnidades -> "Por debajo del promedio"
                else -> "En o sobre el promedio"
            }
            ListItem(
                headlineContent = { Text(producto.nombre) },
                supportingContent = { Text("${formato.format(producto.unidades)} unidades · ${producto.categoria} · $estado") },
                trailingContent = { Text(formato.format(producto.ingreso)) }
            )
        }

        item { Titulo("Clientes compradores", "Top 10 por pedidos registrados en el periodo elegido") }
        if (!pedidosSqlDisponibles || idEmpresa == null) item { Text(mensajePedidos.ifBlank { "No fue posible validar los clientes de este proveedor." }) }
        else if (clientes.isEmpty()) item { Text("No hay clientes con pedidos en el periodo elegido.") }
        else items(clientes) { cliente ->
            ListItem(
                headlineContent = { Text(cliente.nombre) },
                supportingContent = {
                    val producto = cliente.productoMasSolicitado?.let { " · más solicitado: $it" }.orEmpty()
                    Text("${cliente.pedidos} pedidos · última compra ${cliente.ultimaCompra}$producto")
                },
                trailingContent = { Text(cliente.id.takeLast(5)) }
            )
        }
    }
}

@Composable
private fun Titulo(titulo: String, subtitulo: String? = null) {
    Column {
        Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (subtitulo != null) Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TarjetaCategoria(categoria: CategoriaResumen, formato: NumberFormat, maximo: Double) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(categoria.nombre, fontWeight = FontWeight.SemiBold)
                Text(formato.format(categoria.ingreso))
            }
            Spacer(Modifier.height(8.dp))
            BarraProporcional((categoria.ingreso / maximo).toFloat())
            Text("${formato.format(categoria.unidades)} unidades", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FilaConteo(titulo: String, cantidad: Int, maximo: Int, unidad: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(titulo)
                Text("$cantidad $unidad", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            BarraProporcional(cantidad.toFloat() / maximo.coerceAtLeast(1))
        }
    }
}

@Composable
private fun BarraProporcional(progreso: Float) {
    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(progreso.coerceIn(0f, 1f)).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
    }
}

@Composable
private fun GraficoBarras(etiquetas: List<String>, valores: List<Double>, formato: NumberFormat) {
    val maximo = valores.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        etiquetas.zip(valores).forEach { (etiqueta, valor) ->
            Column(Modifier.width(82.dp), verticalArrangement = Arrangement.Bottom) {
                Text(formato.format(valor), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Surface(
                    Modifier.fillMaxWidth().height((120.0 * valor / maximo).coerceAtLeast(4.0).toFloat().dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ) {}
                Spacer(Modifier.height(6.dp))
                Text(etiqueta, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}

private fun inicioPeriodo(periodo: PeriodoVentas, hoy: Calendar): Calendar =
    (hoy.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        when (periodo) {
            PeriodoVentas.MES -> set(Calendar.DAY_OF_MONTH, 1)
            PeriodoVentas.TRES_MESES -> { set(Calendar.DAY_OF_MONTH, 1); add(Calendar.MONTH, -2) }
            PeriodoVentas.ANIO -> { set(Calendar.DAY_OF_YEAR, 1) }
            PeriodoVentas.HISTORICO -> { set(1753, Calendar.JANUARY, 1) }
        }
    }

private fun normalizarFecha(fechaHora: String?): String? {
    val fecha = fechaHora?.take(10) ?: return null
    return try {
        formatoDiaIso.isLenient = false
        formatoDiaIso.parse(fecha)
        fecha
    } catch (_: Exception) { null }
}

private fun inicioSemana(fechaIso: String): Calendar? {
    val date = try { formatoDiaIso.parse(fechaIso) } catch (_: Exception) { null } ?: return null
    return Calendar.getInstance().apply {
        time = date
        firstDayOfWeek = Calendar.MONDAY
        val offset = (get(Calendar.DAY_OF_WEEK) + 5) % 7
        add(Calendar.DAY_OF_YEAR, -offset)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
}

private fun ventasSemanales(pedidos: List<PedidoMedible>, inicio: String, hoy: String): List<Pair<String, Double>> {
    if (pedidos.isEmpty()) return emptyList()
    val sumas = pedidos.groupBy { inicioSemana(it.fecha)?.let { c -> formatoDiaIso.format(c.time) } }
        .mapNotNull { (semana, filas) -> semana?.let { it to filas.sumOf(PedidoMedible::total) } }.toMap()
    val cursor = inicioSemana(inicio) ?: return emptyList()
    val ultima = inicioSemana(hoy) ?: return emptyList()
    val semanas = mutableListOf<Pair<String, Double>>()
    while (!cursor.after(ultima)) {
        val clave = formatoDiaIso.format(cursor.time)
        semanas += SimpleDateFormat("dd MMM", Locale.getDefault()).format(cursor.time) to (sumas[clave] ?: 0.0)
        cursor.add(Calendar.DAY_OF_YEAR, 7)
    }
    return semanas.takeLast(8)
}

private fun ventasDiariasSemanaActual(pedidos: List<PedidoMedible>, hoy: Calendar): List<Pair<String, Double>> {
    val inicio = inicioSemana(formatoDiaIso.format(hoy.time)) ?: return emptyList()
    val sumas = pedidos.groupBy { it.fecha }.mapValues { (_, filas) -> filas.sumOf(PedidoMedible::total) }
    val nombres = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val indiceHoy = (hoy.get(Calendar.DAY_OF_WEEK) + 5) % 7
    return (0..indiceHoy).map { indice ->
        val fecha = (inicio.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, indice) }
        nombres[indice] to (sumas[formatoDiaIso.format(fecha.time)] ?: 0.0)
    }
}

private fun promediosPorDiaSemana(pedidos: List<PedidoMedible>, inicio: Calendar, hoy: Calendar): List<Pair<String, Double>> {
    if (pedidos.isEmpty()) return emptyList()
    val sumas = pedidos.groupBy { it.fecha }.mapValues { (_, filas) -> filas.sumOf(PedidoMedible::total) }
    val cursor = inicio.clone() as Calendar
    val importes = DoubleArray(7)
    val dias = IntArray(7)
    while (!cursor.after(hoy)) {
        val indice = (cursor.get(Calendar.DAY_OF_WEEK) + 5) % 7
        importes[indice] += sumas[formatoDiaIso.format(cursor.time)] ?: 0.0
        dias[indice]++
        cursor.add(Calendar.DAY_OF_YEAR, 1)
    }
    val nombres = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    return nombres.indices.map { nombres[it] to if (dias[it] == 0) 0.0 else importes[it] / dias[it] }
}




