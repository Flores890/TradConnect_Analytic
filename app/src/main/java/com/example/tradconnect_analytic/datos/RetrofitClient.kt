package com.example.tradconnect_analytic.datos

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL

/** Configura el cliente HTTP compartido y resuelve recursos publicados por la API. */
object RetrofitClient {
    // Para Azure cambia esta única URL por la dirección HTTPS publicada.
    private const val BASE_URL = "http://10.39.239.249:8000/api/"
    internal val apiBaseUrl: String get() = BASE_URL
    private val serverRoot: String get() = BASE_URL.removeSuffix("api/")

    @Volatile
    private var service: ApiService? = null

    val api: ApiService
        get() = service ?: error("RetrofitClient.initialize(context) debe ejecutarse primero.")

    @Synchronized
    fun initialize(context: Context) {
        if (service != null) return
        val client = OkHttpClient.Builder()
            .authenticator(SessionAuthenticator(context.applicationContext))
            .build()
        service = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun resolverImagen(url: String?): String? {
        val valor = url?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return try {
            URL(URL(serverRoot), valor).toString()
        } catch (_: Exception) {
            null
        }
    }
}
