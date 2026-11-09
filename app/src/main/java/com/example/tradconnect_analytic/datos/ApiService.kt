package com.example.tradconnect_analytic.datos

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/my-data/")
    suspend fun obtenerMisDatos(
        @Header("Authorization") authorization: String
    ): Response<MiDataApi>


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

    @GET("metodos-pago/")
    suspend fun obtenerCatalogoMetodosPago(): Response<List<MetodoPagoApi>>

    @GET("nosql/mi-empresa/evaluaciones_proveedores/")
    suspend fun obtenerEvaluacionesProveedor(
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int
    ): Response<PaginaApi<EvaluacionApi>>

    @GET("nosql/mi-empresa/incidencias_entregas/")
    suspend fun obtenerIncidenciasProveedor(
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int
    ): Response<PaginaApi<IncidenciaApi>>

    @GET("nosql/{coleccion}/")
    suspend fun obtenerColeccionAdmin(
        @Header("Authorization") authorization: String,
        @Path("coleccion") coleccion: String,
        @Query("limit") limite: Int = 500,
        @Query("offset") offset: Int = 0
    ): Response<PaginaApi<JsonObject>>
}


