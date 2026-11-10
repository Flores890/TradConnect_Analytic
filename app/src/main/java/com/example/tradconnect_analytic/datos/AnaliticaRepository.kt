package com.example.tradconnect_analytic.datos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/** Carga datos permitidos para la empresa asociada a la sesión del proveedor. */
class AnaliticaRepository(private val api: ApiService) {
    suspend fun cargarDatosProveedor(token: String, desde: String, hasta: String): DatosProveedorApi =
        withContext(Dispatchers.IO) {
            val auth = "Bearer " + token

            // Este recurso devuelve solo la empresa asociada a la sesión; auth/my-data
            // construye un snapshot SQL amplio que la app no utiliza.
            val empresaResponse = api.obtenerEmpresaActual(auth)
            if (!empresaResponse.isSuccessful) {
                throw ApiRequestException(empresaResponse.code(), "No se pudo verificar la empresa del proveedor.")
            }
            val empresa = empresaResponse.body()?.firstOrNull()
            val empresaId = empresa?.id?.takeIf { it.isNotBlank() }
                ?: error("La sesión no tiene una empresa de proveedor verificable.")

            // Esta ruta también valida en servidor que el token sea de un Proveedor.
            val analiticaResponse = api.obtenerAnaliticaProveedor(auth, desde, hasta)
            if (!analiticaResponse.isSuccessful) {
                throw ApiRequestException(analiticaResponse.code(), "Analítica no disponible.")
            }
            val analitica = analiticaResponse.body()
                ?: error("La API devolvió la analítica vacía.")

            val pedidosResponse = try { api.obtenerPedidosProveedor(auth) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { null }
            val pedidosSqlDisponibles = pedidosResponse?.isSuccessful == true && pedidosResponse.body() != null
            if (pedidosResponse?.code() == 401) throw ApiRequestException(401, "La sesión ya no está autorizada.")
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
            if (productosResponse?.code() == 401) throw ApiRequestException(401, "La sesión ya no está autorizada.")
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

            val monedasResponse = try { api.obtenerCatalogoMonedas() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { null }
            val monedas = monedasResponse?.takeIf { it.isSuccessful }?.body().orEmpty()
            val simboloMoneda = monedas.firstOrNull { it.id == analitica.monedaId }
                ?.simbolo?.trim()?.takeIf { it.isNotEmpty() }

            val evaluacionesCarga = cargarPaginasOpcional { offset -> api.obtenerEvaluacionesProveedor(auth, offset) }
            val comentariosCarga = cargarPaginasOpcional { offset -> api.obtenerComentariosProveedor(auth, offset) }
            val incidenciasCarga = cargarPaginasOpcional { offset -> api.obtenerIncidenciasProveedor(auth, offset) }

            DatosProveedorApi(
                empresaId = empresaId,
                analitica = analitica,
                pedidos = pedidos,
                productosCatalogo = productos,
                productosSqlDisponibles = productosSqlDisponibles,
                metodosPago = metodosPago,
                simboloMoneda = simboloMoneda,
                pedidosSqlDisponibles = pedidosSqlDisponibles,
                mensajePedidos = mensajePedidos,
                mensajeProductos = mensajeProductos,
                evaluaciones = evaluacionesCarga.elementos.filter { it.proveedorEmpresaId == empresaId },
                mensajeEvaluaciones = evaluacionesCarga.error,
                comentarios = comentariosCarga.elementos.filter { it.proveedorEmpresaId == empresaId },
                mensajeComentarios = comentariosCarga.error,
                incidencias = incidenciasCarga.elementos.filter { it.proveedorEmpresaId == empresaId },
                mensajeIncidencias = incidenciasCarga.error,
                usuarioNombre = null,
                empresaNombre = empresa.razonSocial?.takeIf { it.isNotBlank() }
            )
        }

    private suspend fun <T> cargarPaginas(
        llamada: suspend (offset: Int) -> retrofit2.Response<PaginaApi<T>>
    ): List<T> {
        val acumulado = mutableListOf<T>()
        var offset = 0
        while (true) {
            val respuesta = llamada(offset)
            if (!respuesta.isSuccessful) throw ApiRequestException(respuesta.code(), "No se pudo cargar la información.")
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
        if (error is ApiRequestException && error.statusCode == 401) throw error
        ResultadoCarga(emptyList(), error.localizedMessage ?: "No fue posible cargar esta sección.")
    }

}

data class DatosProveedorApi(
    val empresaId: String,
    val analitica: AnaliticaProveedorApi,
    val pedidos: List<PedidoSqlApi>,
    val productosCatalogo: List<ProductoCatalogoApi>,
    val productosSqlDisponibles: Boolean,
    val metodosPago: List<MetodoPagoApi>,
    val simboloMoneda: String?,
    val pedidosSqlDisponibles: Boolean,
    val mensajePedidos: String,
    val mensajeProductos: String,
    val evaluaciones: List<EvaluacionApi>,
    val mensajeEvaluaciones: String,
    val comentarios: List<ComentarioApi>,
    val mensajeComentarios: String,
    val incidencias: List<IncidenciaApi>,
    val mensajeIncidencias: String,
    val usuarioNombre: String? = null,
    val empresaNombre: String? = null
)

private data class ResultadoCarga<T>(val elementos: List<T>, val error: String)

class ApiRequestException(val statusCode: Int, message: String) : Exception("$message Código $statusCode.")

