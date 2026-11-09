package com.example.tradconnect_analytic.datos

import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

class AnaliticaRepository(private val api: ApiService) {
    suspend fun cargarDatosProveedor(token: String, desde: String, hasta: String): DatosProveedorApi =
        withContext(Dispatchers.IO) {
            val auth = "Bearer " + token

            // No se carga ningún dato de ventas hasta confirmar la empresa de esta sesión.
            val identidadResponse = api.obtenerMisDatos(auth)
            if (!identidadResponse.isSuccessful) {
                error("No se pudo verificar la empresa del proveedor. Código " + identidadResponse.code())
            }
            val empresaId = identidadResponse.body()?.empresa?.id?.takeIf { it.isNotBlank() }
                ?: error("La sesión no tiene una empresa de proveedor verificable.")

            // Esta ruta también valida en servidor que el token sea de un Proveedor.
            val analiticaResponse = api.obtenerAnaliticaProveedor(auth, desde, hasta)
            if (!analiticaResponse.isSuccessful) {
                error("Analítica no disponible. Código " + analiticaResponse.code())
            }
            val analitica = analiticaResponse.body()
                ?: error("La API devolvió la analítica vacía.")

            val pedidosResponse = try { api.obtenerPedidosProveedor(auth) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { null }
            val pedidosSqlDisponibles = pedidosResponse?.isSuccessful == true && pedidosResponse.body() != null
            val pedidos = if (pedidosSqlDisponibles) {
                // Se excluyen únicamente pedidos cancelados; cualquier otro estado se contabiliza.
                pedidosResponse?.body().orEmpty().filter { pedido ->
                    val estado = pedido.estadoActual.orEmpty().trim()
                    pedido.proveedorId == empresaId &&
                        !estado.equals("Cancelado", ignoreCase = true) &&
                        !estado.equals("Cancelada", ignoreCase = true)
                }
            } else emptyList()
            val mensajePedidos = when {
                pedidosResponse == null -> "No fue posible leer la respuesta de pedidos SQL."
                !pedidosResponse.isSuccessful -> "La ruta de pedidos SQL respondió con código ${pedidosResponse.code()}."
                pedidosResponse.body() == null -> "La ruta de pedidos SQL respondió sin contenido."
                else -> ""
            }

            val productosResponse = try { api.obtenerProductosProveedor(auth) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { null }
            val productosSqlDisponibles = productosResponse?.isSuccessful == true && productosResponse.body() != null
            val productos = if (productosSqlDisponibles) {
                productosResponse?.body().orEmpty().filter { it.empresaId == empresaId }
            } else emptyList()
            val mensajeProductos = when {
                productosResponse == null -> "No fue posible leer la respuesta del catálogo SQL."
                !productosResponse.isSuccessful -> "La ruta de productos respondió con código ${productosResponse.code()}."
                productosResponse.body() == null -> "La ruta de productos respondió sin contenido."
                else -> ""
            }

            val metodosResponse = try { api.obtenerCatalogoMetodosPago() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { null }
            val metodosPago = metodosResponse?.takeIf { it.isSuccessful }?.body().orEmpty()

            val evaluacionesCarga = cargarPaginasOpcional { offset -> api.obtenerEvaluacionesProveedor(auth, offset) }
            val incidenciasCarga = cargarPaginasOpcional { offset -> api.obtenerIncidenciasProveedor(auth, offset) }

            DatosProveedorApi(
                empresaId = empresaId,
                analitica = analitica,
                pedidos = pedidos,
                productosCatalogo = productos,
                productosSqlDisponibles = productosSqlDisponibles,
                metodosPago = metodosPago,
                pedidosSqlDisponibles = pedidosSqlDisponibles,
                mensajePedidos = mensajePedidos,
                mensajeProductos = mensajeProductos,
                evaluaciones = evaluacionesCarga.elementos.filter { it.proveedorEmpresaId == empresaId },
                mensajeEvaluaciones = evaluacionesCarga.error,
                incidencias = incidenciasCarga.elementos.filter { it.proveedorEmpresaId == empresaId },
                mensajeIncidencias = incidenciasCarga.error
            )
        }

    suspend fun cargarDatosAdmin(token: String): DatosAdminApi = withContext(Dispatchers.IO) {
        val auth = "Bearer " + token
        DatosAdminApi(
            evaluaciones = cargarPaginasAdmin(auth, "evaluaciones_proveedores"),
            incidencias = cargarPaginasAdmin(auth, "incidencias_entregas"),
            transacciones = cargarPaginasAdmin(auth, "transacciones_no_exitosas")
        )
    }

    private suspend fun <T> cargarPaginas(
        llamada: suspend (offset: Int) -> retrofit2.Response<PaginaApi<T>>
    ): List<T> {
        val acumulado = mutableListOf<T>()
        var offset = 0
        while (true) {
            val respuesta = llamada(offset)
            if (!respuesta.isSuccessful) error("No se pudo cargar la información. Código ${respuesta.code()}")
            val pagina = respuesta.body() ?: error("La API devolvió una página vacía.")
            acumulado += pagina.results
            if (pagina.results.size < 50) break
            offset += pagina.results.size
            if (offset > 20_000) break
        }
        return acumulado
    }

    private suspend fun <T> cargarPaginasOpcional(
        llamada: suspend (offset: Int) -> retrofit2.Response<PaginaApi<T>>
    ): ResultadoCarga<T> = try {
        ResultadoCarga(cargarPaginas(llamada), "")
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        ResultadoCarga(emptyList(), error.localizedMessage ?: "No fue posible cargar esta sección.")
    }

    private suspend fun cargarPaginasAdmin(auth: String, coleccion: String): List<JsonObject> {
        val acumulado = mutableListOf<JsonObject>()
        var offset = 0
        while (true) {
            val respuesta = api.obtenerColeccionAdmin(auth, coleccion, 500, offset)
            if (!respuesta.isSuccessful) error("Admin no puede cargar $coleccion. Código ${respuesta.code()}")
            val pagina = respuesta.body() ?: error("La API devolvió vacía la colección $coleccion.")
            acumulado += pagina.results
            if (pagina.results.size < 500) break
            offset += pagina.results.size
            if (offset > 50_000) break
        }
        return acumulado
    }
}

data class DatosProveedorApi(
    val empresaId: String,
    val analitica: AnaliticaProveedorApi,
    val pedidos: List<PedidoSqlApi>,
    val productosCatalogo: List<ProductoCatalogoApi>,
    val productosSqlDisponibles: Boolean,
    val metodosPago: List<MetodoPagoApi>,
    val pedidosSqlDisponibles: Boolean,
    val mensajePedidos: String,
    val mensajeProductos: String,
    val evaluaciones: List<EvaluacionApi>,
    val mensajeEvaluaciones: String,
    val incidencias: List<IncidenciaApi>,
    val mensajeIncidencias: String
)

private data class ResultadoCarga<T>(val elementos: List<T>, val error: String)

data class DatosAdminApi(
    val evaluaciones: List<JsonObject>,
    val incidencias: List<JsonObject>,
    val transacciones: List<JsonObject>
)

