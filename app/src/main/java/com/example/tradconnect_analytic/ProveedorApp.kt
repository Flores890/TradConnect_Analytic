package com.example.tradconnect_analytic


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tradconnect_analytic.datos.*
import com.example.tradconnect_analytic.pantallas.*

@Composable
fun ProveedorApp() {
    var pantallaActual by remember { mutableIntStateOf(0) }

    // 1. Historial de puntos para el gráfico interactivo según cada período (Semana, Mes, Año)
    val historialSemana = listOf(
        PuntoVenta("Lun", 15), PuntoVenta("Mar", 22), PuntoVenta("Mié", 18),
        PuntoVenta("Jue", 30), PuntoVenta("Vie", 25), PuntoVenta("Sáb", 45),
        PuntoVenta("Dom", 38)
    )

    val historialMes = listOf(
        PuntoVenta("25 Sept", 14), PuntoVenta("28 Sept", 10), PuntoVenta("01 Oct", 25),
        PuntoVenta("04 Oct", 12), PuntoVenta("07 Oct", 18), PuntoVenta("10 Oct", 8),
        PuntoVenta("13 Oct", 14), PuntoVenta("16 Oct", 42), PuntoVenta("19 Oct", 16),
        PuntoVenta("22 Oct", 88)
    )

    val historialAnio = listOf(
        PuntoVenta("Ene", 400), PuntoVenta("Feb", 450), PuntoVenta("Mar", 520),
        PuntoVenta("Abr", 490), PuntoVenta("May", 480), PuntoVenta("Jun", 530),
        PuntoVenta("Jul", 610), PuntoVenta("Ago", 580), PuntoVenta("Sep", 590),
        PuntoVenta("Oct", 820), PuntoVenta("Nov", 750), PuntoVenta("Dic", 950)
    )

    val datosSemanaMock = DatosPeriodoVentas(
        titulo = "Semana Actual",
        ventasTotalPeriodo = 193,
        estadoComparativo = "+12% superior a la semana pasada",
        puntosHistorial = historialSemana
    )

    val datosMesMock = DatosPeriodoVentas(
        titulo = "Mes Actual",
        ventasTotalPeriodo = 460,
        estadoComparativo = "Casi igual a lo habitual",
        puntosHistorial = historialMes
    )

    val datosAnioMock = DatosPeriodoVentas(
        titulo = "Año Actual",
        ventasTotalPeriodo = 7170,
        estadoComparativo = "+25% en crecimiento constante",
        puntosHistorial = historialAnio
    )

    // 2. Datos simulados de Evaluaciones
    val evaluacionesMock = listOf(
        EvaluacionCliente("1", "Carlos Mendoza", 5.0, "Excelente servicio y entrega rápida.", "2026-10-20"),
        EvaluacionCliente("2", "Ana Sofía Ruiz", 4.0, "Buen producto, aunque demoró un día más.", "2026-10-18"),
        EvaluacionCliente("3", "Luis Fernando", 5.0, "Muy satisfecho con la atención prestada.", "2026-10-15")
    )

    // 3. Datos simulados de Incidencias
    val incidenciasMock = listOf(
        Incidencia("1", "Retraso en despacho", "El paquete #402 salió con 2 horas de retraso.", "Resuelto"),
        Incidencia("2", "Error en empaque", "Caja con ligero golpe reportado por cliente.", "En revisión")
    )

    // 4. Datos simulados de Comentarios Post-Compra
    val comentariosMock = listOf(
        ComentarioPostCompra("1", "María José", "Me encantó la calidad del material."),
        ComentarioPostCompra("2", "Esteban Ramos", "Todo perfecto, volveré a comprar.")
    )

    // 5. Datos simulados de Necesidades No Satisfechas
    val necesidadesMock = listOf(
        NecesidadNoSatisfecha("1", "Envíos express nocturnos", "Se han reportado 14 solicitudes de entrega en horario nocturno no cubierto."),
        NecesidadNoSatisfecha("2", "Pago con criptomonedas", "8 clientes consultaron por pasarela de pago cripto.")
    )

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.primary) {
                Text(
                    text = "Panel de Analítica - Proveedor",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                )
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
                .padding(padding)
        ) {
            when (pantallaActual) {
                0 -> PantallaGeneral(
                    totalVentasHistoricoCount = 7823,
                    pedidosActivos = 28,
                    clientesActivos = 312,
                    datosSemana = datosSemanaMock,
                    datosMes = datosMesMock,
                    datosAnio = datosAnioMock
                )

                1 -> PantallaEvaluaciones(
                    promedioCalificacion = 4.6,
                    listaEvaluaciones = evaluacionesMock
                )

                2 -> PantallaIncidenciasYComentarios(
                    totalIncidencias = incidenciasMock.size,
                    listaIncidencias = incidenciasMock,
                    listaComentarios = comentariosMock,
                    listaNecesidades = necesidadesMock
                )
            }
        }
    }
}