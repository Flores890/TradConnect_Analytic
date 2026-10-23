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

    // Datos simulados estructurados bajo formato MongoDB para pruebas locales
    val ventasMock = (1..20).map { i ->
        VentaPeriodo(
            id = i.toString(),
            etiquetaPeriodo = "Periodo $i",
            ventasActuales = (50 + i * 12),
            ventasAnterior = (40 + i * 10)
        )
    }

    val evaluacionesMock = listOf(
        EvaluacionCliente("1", "Carlos Mendoza", 5.0, "Excelente servicio y entrega rápida.", "2026-10-20"),
        EvaluacionCliente("2", "Ana Sofía Ruiz", 4.0, "Buen producto, aunque demoró un día más.", "2026-10-18"),
        EvaluacionCliente("3", "Luis Fernando", 5.0, "Muy satisfecho con la atención prestada.", "2026-10-15")
    )

    val incidenciasMock = listOf(
        Incidencia("1", "Retraso en despacho", "El paquete #402 salió con 2 horas de retraso.", "Resuelto"),
        Incidencia("2", "Error en empaque", "Caja con ligero golpe reportado por cliente.", "En revisión")
    )

    val comentariosMock = listOf(
        ComentarioPostCompra("1", "María José", "Me encantó la calidad del material."),
        ComentarioPostCompra("2", "Esteban Ramos", "Todo perfecto, volveré a comprar.")
    )

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
                        .padding(16.dp)
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
                    totalHistorico = 45820.50,
                    pedidosActivos = 28,
                    clientesActivos = 312,
                    listaVentas = ventasMock
                )
                1 -> PantallaEvaluaciones(
                    promedioCalificacion = 4.6,
                    listaEvaluaciones = evaluacionesMock
                )
                2 -> PantallaIncidenciasYComentarios(
                    totalIncidencias = 2,
                    listaIncidencias = incidenciasMock,
                    listaComentarios = comentariosMock,
                    listaNecesidades = necesidadesMock
                )
            }
        }
    }
}