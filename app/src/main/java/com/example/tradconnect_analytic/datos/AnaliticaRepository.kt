package com.example.tradconnect_analytic.datos

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Response

/** Carga datos limitados a la empresa asociada a la sesión del proveedor. */
class AnaliticaRepository(private val api: ApiService) {

    suspend fun cargarDatosProveedor(
        token: String,
        desde: String,
        hasta: String
    ): DatosProveedorApi = withContext(Dispatchers.IO) {
        coroutineScope {
            val auth = "Bearer $token"

            // Estas dos rutas son independientes: la analítica aplica el alcance del JWT.
            val empresaDeferred = async { api.obtenerEmpresaActual(auth) }
            val analiticaDeferred = async {
                api.obtenerAnaliticaProveedor(auth, desde, hasta)
            }

            // Las demás se consultan en paralelo para evitar sumar sus latencias.
            val pedidosDeferred = async {
                cargarRespuestaOpcional { api.obtenerPedidosProveedor(auth) }
            }
            val productosDeferred = async {
                cargarRespuestaOpcional { api.obtenerProductosProveedor(auth) }
            }
            val metodosDeferred = async {
                cargarRespuestaOpcional { api.obtenerCatalogoMetodosPago() }
            }
            val monedasDeferred = async {
                cargarRespuestaOpcional { api.obtenerCatalogoMonedas() }
            }
            val evaluacionesDeferred = async {
                cargarPaginasOpcional { offset ->
                    api.obtenerEvaluacionesProveedor(auth, offset)
                }
            }
            val comentariosDeferred = async {
                cargarPaginasOpcional { offset ->
                    api.obtenerComentariosProveedor(auth, offset)
                }
            }
            val incidenciasDeferred = async {
                cargarPaginasOpcional { offset ->
                    api.obtenerIncidenciasProveedor(auth, offset)
                }
            }

            val empresaResponse = empresaDeferred.await()
            if (!empresaResponse.isSuccessful) {
                throw ApiRequestException(
                    empresaResponse.code(),
                    "No se pudo verificar la empresa del proveedor."
                )
            }

            val empresa = empresaResponse.body()?.firstOrNull()
            val empresaId = empresa?.id?.takeIf { it.isNotBlank() }
                ?: error("La sesión no tiene una empresa de proveedor verificable.")

            val analiticaResponse = analiticaDeferred.await()
            if (!analiticaResponse.isSuccessful) {
                throw ApiRequestException(
                    analiticaResponse.code(),
                    "Analítica no disponible."
                )
            }
            val analitica = analiticaResponse.body()
                ?: error("La API devolvió la analítica vacía.")

            val pedidosCarga = pedidosDeferred.await()
            val pedidosSqlDisponibles = pedidosCarga.datos != null
            val pedidos = pedidosCarga.datos.orEmpty().filter { pedido ->
                val estado = pedido.estadoActual.orEmpty().trim()
                pedido.proveedorId == empresaId &&
                    !estado.equals("Cancelado", ignoreCase = true) &&
                    !estado.equals("Cancelada", ignoreCase = true)
            }

            val productosCarga = productosDeferred.await()
            val productosSqlDisponibles = productosCarga.datos != null
            val productos = productosCarga.datos.orEmpty()
                .filter { it.empresaId == empresaId }

            val metodosPago = metodosDeferred.await().datos.orEmpty()
            val monedas = monedasDeferred.await().datos.orEmpty()
            val simboloMoneda = monedas.firstOrNull { it.id == analitica.monedaId }
                ?.simbolo?.trim()?.takeIf { it.isNotEmpty() }

            val evaluacionesCarga = evaluacionesDeferred.await()
            val comentariosCarga = comentariosDeferred.await()
            val incidenciasCarga = incidenciasDeferred.await()

            DatosProveedorApi(
                empresaId = empresaId,
                analitica = analitica,
                pedidos = pedidos,
                productosCatalogo = productos,
                productosSqlDisponibles = productosSqlDisponibles,
                metodosPago = metodosPago,
                simboloMoneda = simboloMoneda,
                pedidosSqlDisponibles = pedidosSqlDisponibles,
                mensajePedidos = pedidosCarga.error,
                mensajeProductos = productosCarga.error,
                evaluaciones = evaluacionesCarga.elementos
                    .filter { it.proveedorEmpresaId == empresaId },
                mensajeEvaluaciones = evaluacionesCarga.error,
                comentarios = comentariosCarga.elementos
                    .filter { it.proveedorEmpresaId == empresaId },
                mensajeComentarios = comentariosCarga.error,
                incidencias = incidenciasCarga.elementos
                    .filter { it.proveedorEmpresaId == empresaId },
                mensajeIncidencias = incidenciasCarga.error,
                usuarioNombre = null,
                empresaNombre = empresa.razonSocial?.takeIf { it.isNotBlank() }
            )
        }
    }

    private suspend fun <T> cargarRespuestaOpcional(
        llamada: suspend () -> Response<T>
    ): RespuestaOpcional<T> = try {
        val respuesta = llamada()
        if (respuesta.code() == 401) {
            throw ApiRequestException(401, "La sesión ya no está autorizada.")
        }
        when {
            !respuesta.isSuccessful -> RespuestaOpcional(
                datos = null,
                error = "La API respondió con código ${respuesta.code()}."
            )
            respuesta.body() == null -> RespuestaOpcional(
                datos = null,
                error = "La API respondió sin contenido."
            )
            else -> RespuestaOpcional(respuesta.body(), "")
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: ApiRequestException) {
        throw error
    } catch (error: Exception) {
        RespuestaOpcional(
            datos = null,
            error = error.localizedMessage ?: "No fue posible leer esta sección."
        )
    }

    private suspend fun <T> cargarPaginas(
        llamada: suspend (offset: Int) -> Response<PaginaApi<T>>
    ): List<T> {
        val acumulado = mutableListOf<T>()
        var offset = 0
        while (true) {
            val respuesta = llamada(offset)
            if (!respuesta.isSuccessful) {
                throw ApiRequestException(
                    respuesta.code(),
                    "No se pudo cargar la información."
                )
            }
            val pagina = respuesta.body()
                ?: error("La API devolvió una página vacía.")
            acumulado += pagina.results
            if (pagina.results.size < 50) break
            offset += pagina.results.size
            if (offset > 20_000) break
        }
        return acumulado
    }

    private suspend fun <T> cargarPaginasOpcional(
        llamada: suspend (offset: Int) -> Response<PaginaApi<T>>
    ): ResultadoCarga<T> = try {
        ResultadoCarga(cargarPaginas(llamada), "")
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: ApiRequestException) {
        if (error.statusCode == 401) throw error
        ResultadoCarga(emptyList(), error.message.orEmpty())
    } catch (error: Exception) {
        ResultadoCarga(
            emptyList(),
            error.localizedMessage ?: "No fue posible cargar esta sección."
        )
    }
}

private data class RespuestaOpcional<T>(
    val datos: T?,
    val error: String
)

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

private data class ResultadoCarga<T>(
    val elementos: List<T>,
    val error: String
)

class ApiRequestException(
    val statusCode: Int,
    message: String
) : Exception("$message Código $statusCode.")
