package com.example.tradconnect_analytic.pantallas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BadgeAlert
import com.composables.icons.lucide.ChartPie
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.UsersRound
import java.text.NumberFormat
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

internal data class DatosDetalleVentasUi(
    val periodo: PeriodoVentas,
    val categorias: List<CategoriaResumen>,
    val detalleCompleto: Boolean,
    val metodosUsados: List<Pair<String, Int>>,
    val productosPocoSolicitados: List<ProductoResumen>,
    val rankingProductosVendidos: List<ProductoResumen>,
    val clientes: List<ClienteResumen>,
    val pedidosSqlDisponibles: Boolean,
    val empresaId: String?,
    val mensajePedidos: String,
    val formato: NumberFormat,
    val formatoEntero: NumberFormat,
    val formatoCantidad: NumberFormat,
    val monedaCorta: String,
    val formatoMoneda: (Double) -> String
)

@Composable
internal fun GraficoDonaCategorias(categorias: List<CategoriaResumen>, formato: NumberFormat, moneda: String) {
    val totalIngresos = categorias.sumOf { it.ingreso }.coerceAtLeast(0.001)
    var seleccionado by remember(categorias) { mutableIntStateOf(0) }
    val categoriaActiva = categorias.getOrNull(seleccionado) ?: categorias.first()
    val porcentaje = (categoriaActiva.ingreso / totalIngresos * 100.0).roundToInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(210.dp), contentAlignment = Alignment.Center) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(categorias, totalIngresos) {
                            detectTapGestures { toque ->
                                val centroX = size.width / 2f
                                val centroY = size.height / 2f
                                val angulo = (Math.toDegrees(
                                    atan2((toque.y - centroY).toDouble(), (toque.x - centroX).toDouble())
                                ).toFloat() + 90f + 360f) % 360f
                                var acumulado = 0f
                                val indice = categorias.indices.firstOrNull { index ->
                                    val tramo = (categorias[index].ingreso / totalIngresos * 360.0).toFloat()
                                    val dentro = angulo >= acumulado && angulo < acumulado + tramo
                                    acumulado += tramo
                                    dentro
                                }
                                if (indice != null) seleccionado = indice
                            }
                        }
                        .semantics {
                            contentDescription = "Gráfico interactivo de compras por categoría. Toca un segmento o el nombre de una categoría."
                        }
                ) {
                    val grosor = size.minDimension * 0.19f
                    var inicio = -90f
                    categorias.forEachIndexed { index, categoria ->
                        val tramo = (categoria.ingreso / totalIngresos * 360.0).toFloat()
                        val grosorSegmento = grosor + if (index == seleccionado) 7.dp.toPx() else 0f
                        drawArc(
                            color = coloresGrafico[index % coloresGrafico.size],
                            startAngle = inicio,
                            sweepAngle = (tramo - 2f).coerceAtLeast(0.5f),
                            useCenter = false,
                            topLeft = Offset(grosorSegmento / 2f, grosorSegmento / 2f),
                            size = Size(size.width - grosorSegmento, size.height - grosorSegmento),
                            style = Stroke(width = grosorSegmento, cap = StrokeCap.Butt)
                        )
                        inicio += tramo
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${porcentaje}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(categoriaActiva.nombre, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${formato.format(categoriaActiva.ingreso)} $moneda", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                categorias.forEachIndexed { index, categoria ->
                    val elegido = index == seleccionado
                    val participacion = (categoria.ingreso / totalIngresos * 100.0).roundToInt()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (elegido) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                            .clickable(role = Role.Button) { seleccionado = index }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(12.dp).background(coloresGrafico[index % coloresGrafico.size], CircleShape))
                        Spacer(Modifier.width(10.dp))
                        Text(categoria.nombre, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$participacion%", fontWeight = FontWeight.SemiBold)
                            Text("${formato.format(categoria.ingreso)} $moneda", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("›", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (categorias.any { it.nombre == "Categoría no disponible" }) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Algunos productos no tienen una categoría disponible en los datos de la API.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun FilaConteo(titulo: String, cantidad: Int, maximo: Int, unidad: String, color: Color) {
    val progreso = cantidad.toFloat() / maximo.coerceAtLeast(1)
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(color, CircleShape))
                Spacer(Modifier.width(10.dp))
                Text(titulo, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text("$cantidad $unidad", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(9.dp))
            BarraProporcional(progreso, color)
        }
    }
}

@Composable
private fun BarraProporcional(progreso: Float, color: Color) {
    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(progreso.coerceIn(0f, 1f)).clip(CircleShape).background(color))
    }
}

/** Organiza el detalle de ventas por categoría, pago, producto y cliente. */
@Composable
internal fun PantallaDetalleVentas(
    datos: DatosDetalleVentasUi,
    onPeriodoChange: (PeriodoVentas) -> Unit
) {
    val periodo = datos.periodo
    val categorias = datos.categorias
    val detalleCompleto = datos.detalleCompleto
    val metodosUsados = datos.metodosUsados
    val productosPocoSolicitados = datos.productosPocoSolicitados
    val rankingProductosVendidos = datos.rankingProductosVendidos
    val clientes = datos.clientes
    val pedidosSqlDisponibles = datos.pedidosSqlDisponibles
    val idEmpresa = datos.empresaId
    val mensajePedidos = datos.mensajePedidos
    val formato = datos.formato
    val formatoEntero = datos.formatoEntero
    val formatoCantidad = datos.formatoCantidad
    val monedaCorta = datos.monedaCorta
    val formatoMoneda = datos.formatoMoneda
    var mostrarPocoSolicitados by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val filasMetodosPago = maxOf(1, metodosUsados.size)
    val filasProductos = if (!detalleCompleto || rankingProductosVendidos.isEmpty()) 1 else rankingProductosVendidos.size
    val indiceCategoriasDetalle = 3
    val indicePagosDetalle = 5
    val indiceProductosDetalle = indicePagosDetalle + 1 + filasMetodosPago
    val indiceClientesDetalle = indiceProductosDetalle + 1 + filasProductos + if (productosPocoSolicitados.isNotEmpty()) 1 else 0

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
            item { SelectorPeriodo(periodo, onPeriodoChange) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Detalle de ventas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${periodo.titulo} · categorías, pagos, productos y clientes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            modifier = Modifier.weight(1f),
                            onClick = { coroutineScope.launch { listState.animateScrollToItem(indiceCategoriasDetalle) } },
                            label = { Text("Categorías", maxLines = 1) },
                            leadingIcon = { Icon(Lucide.ChartPie, contentDescription = null) }
                        )
                        AssistChip(
                            modifier = Modifier.weight(1f),
                            onClick = { coroutineScope.launch { listState.animateScrollToItem(indicePagosDetalle) } },
                            label = { Text("Pagos", maxLines = 1) },
                            leadingIcon = { Icon(Lucide.CreditCard, contentDescription = null) }
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            modifier = Modifier.weight(1f),
                            onClick = { coroutineScope.launch { listState.animateScrollToItem(indiceProductosDetalle) } },
                            label = { Text("Productos", maxLines = 1) },
                            leadingIcon = { Icon(Lucide.Package, contentDescription = null) }
                        )
                        AssistChip(
                            modifier = Modifier.weight(1f),
                            onClick = { coroutineScope.launch { listState.animateScrollToItem(indiceClientesDetalle) } },
                            label = { Text("Clientes", maxLines = 1) },
                            leadingIcon = { Icon(Lucide.UsersRound, contentDescription = null) }
                        )
                    }
                }
            }

            item { Titulo("Ventas por categoría", "Participación en ingresos; toca una categoría para ver el detalle") }
            if (!detalleCompleto) item {
                Text("La API no devolvió el detalle completo de los pedidos; no se puede calcular esta distribución.")
            }
            else if (categorias.isEmpty()) item { Text("No hay compras por categoría en este periodo.") }
            else item { GraficoDonaCategorias(categorias, formato, monedaCorta) }

            item { Titulo("Métodos de pago utilizados") }
            if (metodosUsados.isEmpty()) item { Text("No hay métodos de pago registrados en este periodo.") }
            else itemsIndexed(metodosUsados) { index, (nombre, cantidad) ->
                FilaConteo(
                    titulo = nombre,
                    cantidad = cantidad,
                    maximo = metodosUsados.maxOf { it.second },
                    unidad = if (cantidad == 1) "venta" else "ventas",
                    color = coloresGrafico[index % coloresGrafico.size]
                )
            }

            item { Titulo("Productos más vendidos") }
            if (productosPocoSolicitados.isNotEmpty()) item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { mostrarPocoSolicitados = !mostrarPocoSolicitados },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Lucide.BadgeAlert, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Requieren atención", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${productosPocoSolicitados.size} con ventas bajo el promedio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(if (mostrarPocoSolicitados) "Ocultar" else "Ver", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                        }
                        if (mostrarPocoSolicitados) productosPocoSolicitados.forEach { producto ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(producto.nombre, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(producto.categoria, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    if (producto.unidades == 0.0) "Sin ventas" else "${formatoCantidad.format(producto.unidades)} ${producto.unidad}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            if (!detalleCompleto) {
                item { Text("La API no devolvió el detalle completo de los pedidos; no se muestra un ranking parcial.") }
            } else if (rankingProductosVendidos.isEmpty()) item { Text("No hay productos vendidos en este periodo.") }
            else itemsIndexed(rankingProductosVendidos) { index, producto ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text("${index + 1}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(producto.nombre, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("${formatoCantidad.format(producto.unidades)} ${producto.unidad} · ${producto.categoria}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(formatoMoneda(producto.ingreso), fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Titulo("Clientes frecuentes", "Frecuencia y compras recientes durante el periodo") }
            if (!pedidosSqlDisponibles || idEmpresa == null) item { Text(mensajePedidos.ifBlank { "No fue posible validar los clientes de este proveedor." }) }
            else if (clientes.isEmpty()) item { Text("No hay clientes con compras en el periodo elegido.") }
            else itemsIndexed(clientes) { index, cliente ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = coloresGrafico[(index + 2) % coloresGrafico.size]) {
                            Text("${index + 1}", modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(cliente.nombre, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "Producto más comprado: ${cliente.productoMasSolicitado ?: "No disponible"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Última compra: ${formatearFechaCorta(cliente.ultimaCompra)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("${formatoEntero.format(cliente.pedidos)} compras", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
    }
}
