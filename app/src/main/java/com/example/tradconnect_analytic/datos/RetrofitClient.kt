package com.example.tradconnect_analytic.datos

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Dirección principal de la API alojada en Azure-- temporalmente en local
    private const val BASE_URL = "http://10.39.239.249:8000/api/"

    // Crear la conexión con Retrofit
    val api: ApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(ApiService::class.java)

    }
}