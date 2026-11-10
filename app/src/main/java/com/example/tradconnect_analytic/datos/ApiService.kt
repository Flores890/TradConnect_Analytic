package com.example.tradconnect_analytic.datos

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/** Define las rutas REST que consume la app móvil. */
interface ApiService {
    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("empresas/")
    suspend fun obtenerEmpresaActual(
        @Header("Authorization") authorization: String
    ): Response<List<EmpresaActualApi>>


    @GET("nosql/proveedor/analitica/")
    suspend fun obtenerAnaliticaProveedor(
        @Header("Authorization") authorization: String,
        @Query("start") fechaInicio: String,
        @Query("end") fechaFin: String
    ): Response<AnaliticaProveedorApi>

    @GET("productos/")
    suspend fun obtenerProductosProveedor(
        @Header("Authorization") authorization: String
    ): Response<List<ProductoCatalogoApi>>

    @GET("pedidos/")
    suspend fun obtenerPedidosProveedor(
        @Header("Authorization") authorization: String
    ): Response<List<PedidoSqlApi>>

    @GET("monedas/")
    suspend fun obtenerCatalogoMonedas(): Response<List<MonedaCatalogoApi>>

    @GET("metodos-pago/")
    suspend fun obtenerCatalogoMetodosPago(): Response<List<MetodoPagoApi>>

    @GET("nosql/mi-empresa/evaluaciones_proveedores/")
    suspend fun obtenerEvaluacionesProveedor(
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int
    ): Response<PaginaApi<EvaluacionApi>>

    @GET("nosql/mi-empresa/comentarios_post_compra/")
    suspend fun obtenerComentariosProveedor(
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int
    ): Response<PaginaApi<ComentarioApi>>

    @GET("nosql/mi-empresa/incidencias_entregas/")
    suspend fun obtenerIncidenciasProveedor(
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int
    ): Response<PaginaApi<IncidenciaApi>>

}


