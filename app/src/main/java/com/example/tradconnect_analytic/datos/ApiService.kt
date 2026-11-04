package com.example.tradconnect_analytic.datos

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // --- AUTENTICACIÓN ---
    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // --- EVALUACIONES DE PROVEEDOR ---
    @GET("nosql/evaluaciones_proveedores/")
    suspend fun obtenerEvaluacionesProveedor(): Response<List<EvaluacionCliente>>
}
