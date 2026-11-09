package com.example.tradconnect_analytic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tradconnect_analytic.datos.AnaliticaRepository
import com.example.tradconnect_analytic.datos.DatosAdminApi
import com.example.tradconnect_analytic.datos.DatosProveedorApi
import com.example.tradconnect_analytic.datos.EvaluacionCliente
import com.example.tradconnect_analytic.datos.Incidencia
import com.example.tradconnect_analytic.datos.RetrofitClient
import com.example.tradconnect_analytic.datos.TokenManager
import com.example.tradconnect_analytic.pantallas.PantallaEvaluaciones
import com.example.tradconnect_analytic.pantallas.PantallaIncidencias
import com.example.tradconnect_analytic.pantallas.PantallaVentasApi
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TradConnectApp(
    role: String,
    onLogout: () -> Unit
) {
    var pantallaActual by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var accessToken by remember { mutableStateOf(tokenManager.getToken()) }
    val repository = remember { AnaliticaRepository(RetrofitClient.api) }
    val rolNormalizado = role.trim().uppercase(Locale.ROOT)

    var datosProveedor by remember { mutableStateOf<DatosProveedorApi?>(null) }
    var datosAdmin by remember { mutableStateOf<DatosAdminApi?>(null) }
    var listaEvaluaciones by remember {
        mutableStateOf<List<EvaluacionCliente>>(emptyList())
    }
    var listaIncidencias by remember {
        mutableStateOf<List<Incidencia>>(emptyList())
    }
    var promedioCalificacion by remember { mutableStateOf(0.0) }

    LaunchedEffect(accessToken, rolNormalizado) {
        isLoading = true
        errorMessage = null
        datosProveedor = null
        datosAdmin = null
        listaEvaluaciones = emptyList()
        listaIncidencias = emptyList()
        promedioCalificacion = 0.0

        if (accessToken.isNullOrBlank()) {
            errorMessage = "La sesión terminó. Inicia sesión nuevamente."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            when (rolNormalizado) {
                "PROVEEDOR" -> {
                    val fechaFin = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val resultado = repository.cargarDatosProveedor(
                        token = accessToken!!,
                        desde = "1753-01-01",
                        hasta = fechaFin
                    )
                    datosProveedor = resultado

                    listaEvaluaciones = resultado.evaluaciones.map { evaluacion ->
                        EvaluacionCliente(
                            id = evaluacion.id.orEmpty(),
                            clienteNombre = evaluacion.cliente.orEmpty().ifBlank { "Cliente" },
                            puntuacion = evaluacion.puntuacion ?: 0.0,
                            comentario = evaluacion.comentario,
                            fecha = evaluacion.fecha.orEmpty(),
                            calidad = evaluacion.calidad,
                            tiempoEntrega = evaluacion.tiempoEntrega,
                            precio = evaluacion.precio,
                            atencion = evaluacion.atencion
                        )
                    }
                    promedioCalificacion = listaEvaluaciones
                        .map { it.puntuacion }
                        .takeIf { it.isNotEmpty() }
                        ?.average()
                        ?: 0.0

                    listaIncidencias = resultado.incidencias.map { incidencia ->
                        Incidencia(
                            id = incidencia.id.orEmpty(),
                            titulo = incidencia.tipo ?: "Incidencia",
                            descripcion = incidencia.reclamo.orEmpty(),
                            estado = incidencia.estado.orEmpty(),
                            fecha = incidencia.fecha.orEmpty(),
                            fechaRespuesta = incidencia.fechaRespuesta.orEmpty()
                        )
                    }
                }

                "ADMIN", "ADMINISTRADOR" -> {
                    datosAdmin = repository.cargarDatosAdmin(accessToken!!)
                }

                else -> {
                    errorMessage =
                        "Este usuario no cuenta con acceso a los módulos analíticos."
                }
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage
                ?: "No se pudieron cargar los datos. Comprueba la conexión con la API."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.primary) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TradConnect Analytic",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
TextButton(onClick = onLogout) {
                        Text("Salir", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pantallaActual == 0,
                    onClick = { pantallaActual = 0 },
                    icon = { Text("📊") },
                    label = { Text("General") }
                )
                NavigationBarItem(
                    selected = pantallaActual == 1,
                    onClick = { pantallaActual = 1 },
                    icon = { Text("⭐") },
                    label = { Text("Evaluaciones") }
                )
                NavigationBarItem(
                    selected = pantallaActual == 2,
                    onClick = { pantallaActual = 2 },
                    icon = { Text("⚠️") },
                    label = { Text("Incidencias") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> {
                    Text(
                        errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(20.dp)
                    )
                }
                rolNormalizado == "PROVEEDOR" -> {
                    when (pantallaActual) {
                        0 -> {
                            val analitica = datosProveedor?.analitica
                            if (analitica == null) {
                                Text("La API no devolvió datos de ventas.")
                            } else {
                                PantallaVentasApi(
                                    datos = analitica,
                                    pedidos = datosProveedor?.pedidos.orEmpty(),
                                    productosCatalogo = datosProveedor?.productosCatalogo.orEmpty(),
                                    metodosPago = datosProveedor?.metodosPago.orEmpty(),
                                    empresaId = datosProveedor?.empresaId,
                                    productosSqlDisponibles = datosProveedor?.productosSqlDisponibles == true,
                                    pedidosSqlDisponibles =
                                        datosProveedor?.pedidosSqlDisponibles == true,
                                    mensajePedidos =
                                        datosProveedor?.mensajePedidos.orEmpty(),
                                    mensajeProductos = datosProveedor?.mensajeProductos.orEmpty()
                                )
                            }
                        }
                        1 -> PantallaEvaluaciones(
                            promedioCalificacion = promedioCalificacion,
                            listaEvaluaciones = listaEvaluaciones,
                            mensajeCarga = datosProveedor?.mensajeEvaluaciones.orEmpty()
                        )
                        2 -> PantallaIncidencias(listaIncidencias, datosProveedor?.mensajeIncidencias.orEmpty())
                    }
                }
                rolNormalizado == "ADMIN" || rolNormalizado == "ADMINISTRADOR" -> {
                    val datos = datosAdmin
                    if (datos == null) {
                        Text("No hay datos administrativos disponibles.")
                    } else {
                        when (pantallaActual) {
                            0 -> ResumenAdmin(datos)
                            1 -> ResumenModuloAdmin(
                                "Evaluaciones de proveedores",
                                listOf(
                                    "Evaluaciones: " + datos.evaluaciones.size
                                )
                            )
                            2 -> ResumenTransaccionesFallidas(datos.transacciones)
                        }
                    }
                }
                else -> Text("No hay una vista disponible para este rol.")
            }
        }
    }

}

@Composable
private fun ResumenAdmin(datos: DatosAdminApi) {
    ResumenModuloAdmin(
        "Resumen global de MongoDB",
        listOf(
            "Evaluaciones: " + datos.evaluaciones.size,
            "Incidencias: " + datos.incidencias.size,
            "Transacciones no exitosas: " + datos.transacciones.size
        )
    )
}

@Composable
private fun ResumenTransaccionesFallidas(transacciones: List<JsonObject>) {
    val porProveedor = transacciones
        .groupingBy { doc ->
            doc.texto("proveedor_nombre").ifBlank { "Proveedor sin nombre" }
        }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .take(10)

    ResumenModuloAdmin(
        "Transacciones no exitosas por proveedor",
        if (porProveedor.isEmpty()) {
            listOf("No hay transacciones no exitosas registradas.")
        } else {
            porProveedor.map { (nombre, cantidad) ->
                nombre + ": " + cantidad
            }
        }
    )
}

private fun JsonObject.texto(campo: String): String {
    val valor = get(campo) ?: return ""
    if (valor.isJsonNull || !valor.isJsonPrimitive) return ""
    return runCatching { valor.asString }.getOrDefault("")
}

@Composable
private fun ResumenModuloAdmin(
    titulo: String,
    lineas: List<String>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(titulo, style = MaterialTheme.typography.titleLarge)
        }
        items(lineas.size) { index ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    lineas[index],
                    modifier = Modifier.padding(18.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "La información administrativa es global y solo la consulta el rol Admin.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}




