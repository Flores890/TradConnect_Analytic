package com.example.tradconnect_analytic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChartNoAxesCombined
import com.composables.icons.lucide.ChartPie
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.TriangleAlert
import com.example.tradconnect_analytic.datos.AnaliticaRepository
import com.example.tradconnect_analytic.datos.ApiRequestException
import com.example.tradconnect_analytic.datos.ComentarioPostCompra
import com.example.tradconnect_analytic.datos.DatosProveedorApi
import com.example.tradconnect_analytic.datos.EvaluacionCliente
import com.example.tradconnect_analytic.datos.Incidencia
import com.example.tradconnect_analytic.datos.RetrofitClient
import com.example.tradconnect_analytic.datos.TokenManager
import com.example.tradconnect_analytic.pantallas.PantallaEvaluaciones
import com.example.tradconnect_analytic.pantallas.PantallaIncidencias
import com.example.tradconnect_analytic.pantallas.PantallaVentasApi
import com.example.tradconnect_analytic.pantallas.PeriodoVentas
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun TradConnectApp(
    role: String,
    onLogout: () -> Unit
) {
    var pantallaActual by remember { mutableIntStateOf(0) }
    var periodoVentas by remember { mutableStateOf(PeriodoVentas.HISTORICO) }
    var retryVersion by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var accessToken by remember { mutableStateOf(tokenManager.getToken()) }
    val repository = remember { AnaliticaRepository(RetrofitClient.api) }
    val rolNormalizado = role.trim().uppercase(Locale.ROOT)
    var nombreUsuario by remember { mutableStateOf(tokenManager.getUserName().orEmpty()) }
    var nombreEmpresa by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var datosProveedor by remember { mutableStateOf<DatosProveedorApi?>(null) }
    var listaEvaluaciones by remember {
        mutableStateOf<List<EvaluacionCliente>>(emptyList())
    }
    var listaComentarios by remember { mutableStateOf<List<ComentarioPostCompra>>(emptyList()) }
    var listaIncidencias by remember {
        mutableStateOf<List<Incidencia>>(emptyList())
    }
    var promedioCalificacion by remember { mutableStateOf(0.0) }

    LaunchedEffect(accessToken, rolNormalizado, retryVersion) {
        isLoading = true
        errorMessage = null
        datosProveedor = null
        listaEvaluaciones = emptyList()
        listaComentarios = emptyList()
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
                    nombreEmpresa = resultado.empresaNombre
                    if (!resultado.usuarioNombre.isNullOrBlank()) {
                        nombreUsuario = resultado.usuarioNombre
                    }

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

                    listaComentarios = resultado.comentarios.map { comentario ->
                        ComentarioPostCompra(
                            id = comentario.id.orEmpty(),
                            autor = comentario.cliente.orEmpty().ifBlank { "Cliente" },
                            opinion = comentario.opinion.orEmpty(),
                            calificacion = comentario.calificacion,
                            fecha = comentario.fecha.orEmpty()
                        )
                    }

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

                else -> {
                    tokenManager.clear()
                    onLogout()
                    return@LaunchedEffect
                }
            }
        } catch (e: Exception) {
            if (e is ApiRequestException && e.statusCode == 401 && tokenManager.getToken().isNullOrBlank()) {
                onLogout()
                return@LaunchedEffect
            }
            errorMessage = when {
                e is ApiRequestException && e.statusCode == 401 ->
                    "No fue posible renovar la sesión. Comprueba la conexión y vuelve a intentar."
                else -> e.localizedMessage
                    ?: "No se pudieron cargar los datos. Comprueba la conexión con la API."
            }
        } finally {
            isLoading = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.widthIn(max = 340.dp)) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp)
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFF292C33)).padding(20.dp)
                    ) {
                        Text("TU PERFIL", color = Color(0xFFC6C5F4), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            nombreUsuario.ifBlank { "Usuario" },
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            nombreEmpresa?.takeIf { it.isNotBlank() } ?: "Empresa no disponible",
                            color = Color(0xFFD0D1D7),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            role.trim().replaceFirstChar { it.uppercase() },
                            color = Color(0xFFD9D8FA),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    NavigationDrawerItem(
                        label = { Text("Power BI") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                runCatching { uriHandler.openUri(URL_POWER_BI) }
                            }
                        },
                        icon = { Icon(Lucide.ChartNoAxesCombined, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("TradConnect web · acceso temporal") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                runCatching { uriHandler.openUri(URL_SITIO_TRADCONNECT) }
                            }
                        },
                        icon = { Icon(Lucide.Globe, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                onLogout()
                            }
                        },
                        icon = { Icon(Lucide.LogOut, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
      Scaffold(
        topBar = {
            Surface(color = Color(0xFF292C33)) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TradConnect Analytic",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Lucide.Menu,
                            contentDescription = "Abrir menú lateral",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        bottomBar = {
            val coloresNavegacion = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = pantallaActual == 0,
                    onClick = { pantallaActual = 0 },
                    icon = { Icon(Lucide.ChartNoAxesCombined, contentDescription = null) },
                    label = { Text("General") },
                    colors = coloresNavegacion
                )
                NavigationBarItem(
                    selected = pantallaActual == 3,
                    onClick = { pantallaActual = 3 },
                    icon = { Icon(Lucide.ChartPie, contentDescription = null) },
                    label = { Text("Detalle") },
                    colors = coloresNavegacion
                )
                NavigationBarItem(
                    selected = pantallaActual == 1,
                    onClick = { pantallaActual = 1 },
                    icon = { Icon(Lucide.Star, contentDescription = null) },
                    label = { Text("Evaluaciones") },
                    colors = coloresNavegacion
                )
                NavigationBarItem(
                    selected = pantallaActual == 2,
                    onClick = { pantallaActual = 2 },
                    icon = { Icon(Lucide.TriangleAlert, contentDescription = null) },
                    label = { Text("Incidencias") },
                    colors = coloresNavegacion
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
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { retryVersion++ }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {
                    when (pantallaActual) {
                        0, 3 -> {
                            val analitica = datosProveedor?.analitica
                            if (analitica == null) {
                                Text("La API no devolvió datos de ventas.")
                            } else {
                                PantallaVentasApi(
                                    datos = analitica,
                                    pedidos = datosProveedor?.pedidos.orEmpty(),
                                    productosCatalogo = datosProveedor?.productosCatalogo.orEmpty(),
                                    metodosPago = datosProveedor?.metodosPago.orEmpty(),
                                    simboloMoneda = datosProveedor?.simboloMoneda,
                                    empresaId = datosProveedor?.empresaId,
                                    nombreEmpresa = datosProveedor?.empresaNombre,
                                    periodo = periodoVentas,
                                    onPeriodoChange = { periodoVentas = it },
                                    mostrarDetalle = pantallaActual == 3,
                                    pedidosSqlDisponibles =
                                        datosProveedor?.pedidosSqlDisponibles == true,
                                    mensajePedidos =
                                        datosProveedor?.mensajePedidos.orEmpty()
                                )
                            }
                        }
                        1 -> PantallaEvaluaciones(
                            promedioCalificacion = promedioCalificacion,
                            listaEvaluaciones = listaEvaluaciones,
                            mensajeCarga = datosProveedor?.mensajeEvaluaciones.orEmpty(),
                            listaComentarios = listaComentarios,
                            mensajeComentarios = datosProveedor?.mensajeComentarios.orEmpty()
                        )
                        2 -> PantallaIncidencias(listaIncidencias, datosProveedor?.mensajeIncidencias.orEmpty())
                    }
                }
            }
        }
      }
    }

}

private const val URL_POWER_BI = "https://app.powerbi.com/"
private const val URL_SITIO_TRADCONNECT = URL_POWER_BI


