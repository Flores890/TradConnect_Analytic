package com.example.tradconnect_analytic.pantallas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Receipt
import com.example.tradconnect_analytic.datos.AnaliticaProveedorApi
import com.example.tradconnect_analytic.datos.MetodoPagoApi
import com.example.tradconnect_analytic.datos.PedidoSqlApi
import com.example.tradconnect_analytic.datos.ProductoCatalogoApi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

enum class PeriodoVentas(val titulo: String, val etiquetaCorta: String) {
    SEMANA("Esta semana", "Semana"),
    MES("Este mes", "Mes"),
    ANIO("Este año", "Año"),
    HISTORICO("Todo el histórico", "Todo")
}

private enum class TipoGraficoVentas(val titulo: String) {
    MES("Ventas por mes"),
    ANIO("Comparación anual de ventas")
}

private data class PedidoMedible(
    val pedido: PedidoSqlApi,
    val fecha: String,
    val total: Double
)

internal data class ProductoResumen(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val unidad: String,
    val unidades: Double,
    val ingreso: Double
)

internal data class CategoriaResumen(
    val nombre: String,
    val unidades: Double,
    val ingreso: Double
)

internal data class ClienteResumen(
    val id: String,
    val nombre: String,
    val pedidos: Int,
    val ultimaCompra: String,
    val productoMasSolicitado: String?
)

private val formatoDiaIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
internal val coloresGrafico = listOf(
    Color(0xFF4A8F82),
    Color(0xFF8072A8),
    Color(0xFF5F88A5),
    Color(0xFFB28A52),
    Color(0xFFAA6F78),
    Color(0xFF718E65),
    Color(0xFF657FA6)
)

@Composable
fun PantallaVentasApi(
    datos: AnaliticaProveedorApi,
    pedidos: List<PedidoSqlApi>,
    productosCatalogo: List<ProductoCatalogoApi>,
    metodosPago: List<MetodoPagoApi>,
    simboloMoneda: String?,
    empresaId: String?,
    nombreEmpresa: String?,
    periodo: PeriodoVentas,
    onPeriodoChange: (PeriodoVentas) -> Unit,
    mostrarDetalle: Boolean,
    pedidosSqlDisponibles: Boolean,
    mensajePedidos: String
) {
    val listState = rememberLazyListState()
    val formato = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val formatoCantidad = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    val formatoEntero = NumberFormat.getIntegerInstance(Locale.getDefault())
    val formatoPorcentaje = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }
    val monedaCorta = simboloMoneda?.trim()?.takeIf { it.isNotEmpty() } ?: datos.monedaNombre
    val formatoMoneda: (Double) -> String = { valor -> "${formato.format(valor)} $monedaCorta" }
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
    val ventaMesActualComparable = pedidosContabilizadosConFecha
        .filter { fila ->
            fila.fecha.take(7) == mesActual &&
                (fila.fecha.substring(8, 10).toIntOrNull() ?: 0) <= diaComparableMesAnterior
        }
        .sumOf { it.total }
    val ventaMesAnteriorComparable = pedidosContabilizadosConFecha
        .filter { fila ->
            fila.fecha.take(7) == mesAnterior &&
                (fila.fecha.substring(8, 10).toIntOrNull() ?: 0) <= diaComparableMesAnterior
        }
        .sumOf { it.total }
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
    val lineasSinCategoria = pedidosMedibles.flatMap { it.pedido.detalles }
        .filter { it.productoId == null }
    val detalleCompleto = pedidosMedibles.isEmpty() || pedidosMedibles.all { it.pedido.detalles.isNotEmpty() }
    val lineasPorProducto = lineas.groupBy { it.productoId!! }
    val rankingProductos = if (detalleCompleto) {
        val idsEnCatalogo = mapaProductos.keys
        (lineasPorProducto.keys + idsEnCatalogo).distinct().map { id ->
            val lineasProducto = lineasPorProducto[id].orEmpty()
            val producto = mapaProductos[id]
            ProductoResumen(
                id = id,
                nombre = producto?.nombre.orEmpty().ifBlank {
                    lineasProducto.firstNotNullOfOrNull { it.productoNombre?.takeIf(String::isNotBlank) } ?: "Producto $id"
                },
                categoria = producto?.categoria.orEmpty().ifBlank { "Categoría no disponible" },
                unidad = producto?.unidadNombre?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "Medida no disponible",
                unidades = lineasProducto.sumOf { it.cantidad?.toDoubleOrNull() ?: 0.0 },
                ingreso = lineasProducto.sumOf { it.subtotalLinea?.toDoubleOrNull() ?: 0.0 }
            )
        }.sortedWith(compareByDescending<ProductoResumen> { it.unidades }.thenByDescending { it.ingreso })
    } else emptyList()
    val productosConVenta = rankingProductos.filter { it.unidades > 0.0 }
    val rankingProductosVendidos = productosConVenta
    val promedioUnidades = productosConVenta.map { it.unidades }.average().takeIf { !it.isNaN() } ?: 0.0
    val productosPocoSolicitados = if (promedioUnidades > 0.0) {
        rankingProductos.filter { it.unidades < promedioUnidades }
            .sortedWith(compareBy<ProductoResumen> { it.unidades }.thenBy { it.nombre })
    } else emptyList()

    val categorias = (rankingProductos.map { it.categoria to (it.unidades to it.ingreso) } +
        lineasSinCategoria.map {
            "Categoría no disponible" to (
                (it.cantidad?.toDoubleOrNull() ?: 0.0) to
                    (it.subtotalLinea?.toDoubleOrNull() ?: 0.0)
                )
        })
        .filter { it.second.second > 0.0 }
        .groupBy { it.first }
        .map { (nombre, productos) ->
            CategoriaResumen(
                nombre,
                productos.sumOf { it.second.first },
                productos.sumOf { it.second.second }
            )
        }
        .sortedByDescending { it.ingreso }

    val ventasSemana = ventasSemanales(pedidosMedibles, inicioIso, hoyIso)
    val ventasDiaSemana = ventasDiariasSemanaActual(pedidosMedibles, hoy)

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

    if (mostrarDetalle) {
        PantallaDetalleVentas(
            datos = DatosDetalleVentasUi(
                periodo = periodo,
                categorias = categorias,
                detalleCompleto = detalleCompleto,
                metodosUsados = metodosUsados,
                productosPocoSolicitados = productosPocoSolicitados,
                rankingProductosVendidos = rankingProductosVendidos,
                clientes = clientes,
                pedidosSqlDisponibles = pedidosSqlDisponibles,
                empresaId = idEmpresa,
                mensajePedidos = mensajePedidos,
                formato = formato,
                formatoEntero = formatoEntero,
                formatoCantidad = formatoCantidad,
                monedaCorta = monedaCorta,
                formatoMoneda = formatoMoneda
            ),
            onPeriodoChange = onPeriodoChange
        )
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Tu empresa", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        nombreEmpresa?.takeIf { it.isNotBlank() } ?: "Empresa no disponible",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier.weight(1f).heightIn(min = 148.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text("Ventas realizadas este mes", style = MaterialTheme.typography.labelLarge, maxLines = 2)
                            Spacer(Modifier.height(7.dp))
                            Text(formatoMoneda(ventaMesActual), fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            Text(
                                if (pedidosMesActual == 1) "1 venta" else "${formatoEntero.format(pedidosMesActual)} ventas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f).heightIn(min = 148.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text("Comparación de mes", style = MaterialTheme.typography.labelLarge, maxLines = 2)
                            Spacer(Modifier.height(7.dp))
                            when {
                                !pedidosSqlDisponibles -> Text("Sin datos para comparar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                ventaMesAnteriorComparable > 0.0 || ventaMesActualComparable > 0.0 -> {
                                    val diferencia = ventaMesActualComparable - ventaMesAnteriorComparable
                                    val porcentaje = if (ventaMesAnteriorComparable > 0.0) diferencia / ventaMesAnteriorComparable * 100.0 else null
                                    val colorCambio = when {
                                        diferencia > 0.005 -> MaterialTheme.colorScheme.primary
                                        diferencia < -0.005 -> Color(0xFF9B6A3C)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                    val mensajeCambio = when {
                                        porcentaje != null -> (if (porcentaje > 0.0) "+" else "") + formatoPorcentaje.format(porcentaje) + "%"
                                        diferencia > 0.005 -> "Nuevo ingreso"
                                        else -> "Sin base anterior"
                                    }
                                    Text(mensajeCambio, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colorCambio, maxLines = 1, softWrap = false)
                                    Text(
                                        "${formatoMoneda(ventaMesActualComparable)} este mes · ${formatoMoneda(ventaMesAnteriorComparable)} antes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                else -> Text("Sin ventas comparables", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            if (!pedidosSqlDisponibles) item {
                Text(mensajePedidos.ifBlank { "La ruta de pedidos no devolvió datos válidos." }, color = MaterialTheme.colorScheme.error)
            }
            item { SelectorPeriodo(periodo, onPeriodoChange) }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Ingreso promedio por pedido", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            if (numeroPedidosPeriodo == 0) {
                                Text("—", color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                            } else {
                                Text(formatoMoneda(ticketPromedio), color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Icon(Lucide.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                    }
                }
            }
            item { Titulo("Evolución de ventas", "La gráfica y los indicadores usan el periodo seleccionado") }
            when (periodo) {
                PeriodoVentas.SEMANA -> {
                    item { Text("Ventas de la semana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    if (!pedidosSqlDisponibles || idEmpresa == null) item { Text(mensajePedidos.ifBlank { "No hay datos suficientes para mostrar ventas semanales." }) }
                    else if (pedidosMedibles.isEmpty()) item { Text("Sin ventas registradas esta semana.") }
                    else item { GraficoBarras(ventasDiaSemana.map { it.first }, ventasDiaSemana.map { it.second }, formato, monedaCorta) }
                }
                PeriodoVentas.MES -> {
                    item { Text("Ventas por semana del mes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    if (!pedidosSqlDisponibles || idEmpresa == null) item { Text(mensajePedidos.ifBlank { "No hay datos suficientes para mostrar ventas semanales." }) }
                    else if (pedidosMedibles.isEmpty()) item { Text("Sin ventas semanales en el periodo elegido.") }
                    else if (ventasSemana.isEmpty()) item { Text("No hay semanas con ventas en este mes.") }
                    else item { GraficoBarras(ventasSemana.map { it.first }, ventasSemana.map { it.second }, formato, monedaCorta) }
                }
                PeriodoVentas.ANIO -> {
                    item { Text(TipoGraficoVentas.MES.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    if (ventasMensualesPeriodo.isEmpty()) item { Text("No hay ventas válidas en el periodo seleccionado ($inicioIso a $hoyIso).") }
                    else item {
                        val datosMes = ventasMensualesPeriodo.takeLast(12)
                        GraficoBarras(datosMes.map { etiquetaMes(it.first) }, datosMes.map { it.second }, formato, monedaCorta)
                    }
                }
                PeriodoVentas.HISTORICO -> {
                    item { Text(TipoGraficoVentas.ANIO.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    if (ventasPorAnio.isEmpty()) item { Text("No hay años con ventas disponibles.") }
                    else item { GraficoBarras(ventasPorAnio.keys.toList(), ventasPorAnio.values.toList(), formato, monedaCorta) }
                }
            }
    }
}

@Composable
internal fun Titulo(titulo: String, subtitulo: String? = null) {
    Column {
        Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (subtitulo != null) Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun SelectorPeriodo(periodo: PeriodoVentas, onPeriodoChange: (PeriodoVentas) -> Unit) {
    androidx.compose.material3.PrimaryTabRow(selectedTabIndex = PeriodoVentas.values().indexOf(periodo)) {
        PeriodoVentas.values().forEach { opcion ->
            androidx.compose.material3.Tab(
                selected = periodo == opcion,
                onClick = { onPeriodoChange(opcion) },
                modifier = Modifier.semantics { contentDescription = opcion.titulo },
                text = { Text(opcion.etiquetaCorta, maxLines = 1) }
            )
        }
    }
}

@Composable
private fun GraficoBarras(etiquetas: List<String>, valores: List<Double>, formato: NumberFormat, moneda: String) {
    val maximo = valores.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    var seleccionado by remember(etiquetas, valores) { mutableIntStateOf(-1) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (seleccionado in etiquetas.indices) {
            Text(
                "${etiquetas[seleccionado]} · ${formato.format(valores[seleccionado])} $moneda",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text("Toca una barra para ver su valor", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).height(188.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            etiquetas.zip(valores).forEachIndexed { index, (etiqueta, valor) ->
                val colorBarra = coloresGrafico[index % coloresGrafico.size]
                Column(
                    Modifier.width(58.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(role = Role.Button) { seleccionado = index }
                        .padding(horizontal = 3.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.BottomCenter) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(formato.format(valor), style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                            Spacer(Modifier.height(5.dp))
                            Surface(
                                Modifier.width(24.dp).height((132.0 * valor / maximo).coerceAtLeast(4.0).toFloat().dp),
                                color = colorBarra.copy(alpha = if (seleccionado == index || seleccionado == -1) 1f else 0.45f),
                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            ) {}
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        softWrap = false,
                        color = if (seleccionado == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GraficoLineasDias(etiquetas: List<String>, valores: List<Double>, formato: NumberFormat, moneda: String) {
    var seleccionado by remember(etiquetas, valores) { mutableIntStateOf(-1) }
    val promedioGeneral = valores.average().takeIf { !it.isNaN() } ?: 0.0
    val maximo = valores.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    val colorRejilla = MaterialTheme.colorScheme.outlineVariant

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val textoResumen = if (seleccionado in etiquetas.indices) {
            "Promedio de ${etiquetas[seleccionado]}: ${formato.format(valores[seleccionado])} $moneda · promedio diario general: ${formato.format(promedioGeneral)} $moneda"
        } else {
            "Toca un día para ver su promedio y compáralo con la media diaria general: ${formato.format(promedioGeneral)} $moneda."
        }
        Text(textoResumen, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .pointerInput(etiquetas, valores) {
                    detectTapGestures { toque ->
                        val margen = 20.dp.toPx()
                        val paso = (size.width - margen * 2f) / (valores.size - 1).coerceAtLeast(1)
                        seleccionado = ((toque.x - margen) / paso).roundToInt().coerceIn(valores.indices)
                    }
                }
                .semantics {
                    contentDescription = "Gráfico de líneas del promedio de ventas por día de la semana. Toca un punto para consultar el promedio de ese día."
                }
        ) {
            val margenX = 20.dp.toPx()
            val margenSuperior = 18.dp.toPx()
            val margenInferior = 16.dp.toPx()
            val altoGrafico = (size.height - margenSuperior - margenInferior).coerceAtLeast(1f)
            val anchoGrafico = (size.width - margenX * 2f).coerceAtLeast(1f)
            val pasoX = anchoGrafico / (valores.size - 1).coerceAtLeast(1)
            val puntos = valores.mapIndexed { index, valor ->
                val x = margenX + pasoX * index
                val y = margenSuperior + altoGrafico * (1f - (valor / maximo).toFloat())
                Offset(x, y)
            }

            for (division in 0..4) {
                val y = margenSuperior + altoGrafico * division / 4f
                drawLine(
                    color = colorRejilla,
                    start = Offset(margenX, y),
                    end = Offset(size.width - margenX, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val ruta = androidx.compose.ui.graphics.Path().apply {
                puntos.forEachIndexed { index, punto ->
                    if (index == 0) moveTo(punto.x, punto.y) else lineTo(punto.x, punto.y)
                }
            }
            drawPath(ruta, color = Color(0xFF4F86A6), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            puntos.forEachIndexed { index, punto ->
                val radio = if (seleccionado == index) 7.dp.toPx() else 5.dp.toPx()
                drawCircle(color = Color.White, radius = radio + 2.dp.toPx(), center = punto)
                drawCircle(
                    color = if (seleccionado == index) Color(0xFFD5A15A) else Color(0xFF0F766E),
                    radius = radio,
                    center = punto
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            etiquetas.forEachIndexed { index, etiqueta ->
                Text(
                    etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (seleccionado == index) FontWeight.Bold else FontWeight.Normal,
                    color = if (seleccionado == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun etiquetaMes(mesIso: String): String = try {
    SimpleDateFormat("MMM yy", Locale("es", "GT")).format(SimpleDateFormat("yyyy-MM", Locale.US).parse(mesIso)!!)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "GT")) else it.toString() }
} catch (_: Exception) {
    mesIso
}

internal fun formatearFechaCorta(fechaIso: String): String = try {
    SimpleDateFormat("d MMM yyyy", Locale("es", "GT")).format(formatoDiaIso.parse(fechaIso)!!)
} catch (_: Exception) {
    fechaIso
}

private fun inicioPeriodo(periodo: PeriodoVentas, hoy: Calendar): Calendar =
    (hoy.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        when (periodo) {
            PeriodoVentas.SEMANA -> {
                firstDayOfWeek = Calendar.MONDAY
                val offset = (get(Calendar.DAY_OF_WEEK) + 5) % 7
                add(Calendar.DAY_OF_YEAR, -offset)
            }
            PeriodoVentas.MES -> set(Calendar.DAY_OF_MONTH, 1)
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
    return semanas.takeLast(12)
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




