package com.example.tradconnect_analytic.datos

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Dirección principal de la API alojada en Azure
    private const val BASE_URL =
        "https://tradconnec-api-g9brdvb6bpaddvfz.westus-01.azurewebsites.net/api/"

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