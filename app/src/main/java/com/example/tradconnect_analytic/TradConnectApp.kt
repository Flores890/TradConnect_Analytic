package com.example.tradconnect_analytic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tradconnect_analytic.datos.*
import com.example.tradconnect_analytic.pantallas.*
import kotlinx.coroutines.launch

@Composable
fun TradConnectApp(onLogout: () -> Unit) {
    var pantallaActual by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var listaEvaluaciones by remember { mutableStateOf<List<EvaluacionCliente>>(emptyList()) }
    var listaIncidencias by remember { mutableStateOf<List<Incidencia>>(emptyList()) }
    var listaComentarios by remember { mutableStateOf<List<ComentarioPostCompra>>(emptyList()) }
    var listaNecesidades by remember { mutableStateOf<List<NecesidadNoSatisfecha>>(emptyList()) }
    var promedioCalif by remember { mutableStateOf(0.0) }

    // Descargar datos reales desde Azure al iniciar sesión
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val resEval = RetrofitClient.api.obtenerEvaluacionesProveedor()
                if (resEval.isSuccessful && resEval.body() != null) {
                    listaEvaluaciones = resEval.body()!!
                    if (listaEvaluaciones.isNotEmpty()) {
                        promedioCalif = listaEvaluaciones.map { it.puntuacion }.average()
                    }
                }

                // Si aún no tienes creados estos endpoints en tu ApiService,
                // puedes comentar temporalmente estas líneas o agregarlos cuando gustes.
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error de conexión con Azure: ${e.localizedMessage}"
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.primary) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Panel de Analítica",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    // Datos de respaldo para los gráficos de ventas en PantallaGeneral
                    val historialSemana = listOf(PuntoVenta("Lun", 15), PuntoVenta("Mar", 22), PuntoVenta("Mié", 18), PuntoVenta("Jue", 30), PuntoVenta("Vie", 25), PuntoVenta("Sáb", 45), PuntoVenta("Dom", 38))
                    val historialMes = listOf(PuntoVenta("01 Oct", 25), PuntoVenta("10 Oct", 18), PuntoVenta("22 Oct", 88))
                    val historialAnio = listOf(PuntoVenta("Ene", 400), PuntoVenta("Jun", 530), PuntoVenta("Dic", 950))

                    when (pantallaActual) {
                        0 -> PantallaGeneral(
                            totalVentasHistoricoCount = 7823,
                            pedidosActivos = 28,
                            clientesActivos = 312,
                            datosSemana = DatosPeriodoVentas("Semana Actual", 193, "+12% vs semana pasada", historialSemana),
                            datosMes = DatosPeriodoVentas("Mes Actual", 460, "Estable", historialMes),
                            datosAnio = DatosPeriodoVentas("Año Actual", 7170, "En crecimiento", historialAnio)
                        )

                        1 -> PantallaEvaluaciones(
                            promedioCalificacion = promedioCalif,
                            listaEvaluaciones = listaEvaluaciones
                        )

                        2 -> PantallaIncidenciasYComentarios(
                            totalIncidencias = listaIncidencias.size,
                            listaIncidencias = listaIncidencias,
                            listaComentarios = listaComentarios,
                            listaNecesidades = listaNecesidades
                        )
                    }
                }
            }
        }
    }
}