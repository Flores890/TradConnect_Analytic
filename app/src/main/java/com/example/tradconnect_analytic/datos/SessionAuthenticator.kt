package com.example.tradconnect_analytic.datos

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private interface RefreshService {
    @POST("auth/refresh/")
    suspend fun refresh(@Body request: RefreshTokenRequest): retrofit2.Response<RefreshTokenResponse>
}

/** Renueva el token una vez cuando la API devuelve 401. */
class SessionAuthenticator(context: Context) : Authenticator {
    private val tokenManager = TokenManager(context.applicationContext)
    private val refreshService: RefreshService = Retrofit.Builder()
        .baseUrl(RetrofitClient.apiBaseUrl)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RefreshService::class.java)

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        val requestToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.takeIf { it.isNotBlank() }
            ?: return null

        synchronized(refreshLock) {
            val currentToken = tokenManager.getToken() ?: return null
            if (currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refresh = tokenManager.getRefreshToken() ?: run {
                tokenManager.clear()
                return null
            }

            return try {
                val refreshed = runBlocking(Dispatchers.IO) {
                    refreshService.refresh(RefreshTokenRequest(refresh))
                }
                val newAccess = refreshed.body()?.access
                if (!refreshed.isSuccessful || newAccess.isNullOrBlank()) {
                    tokenManager.clear()
                    null
                } else {
                    tokenManager.updateAccessToken(newAccess)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccess")
                        .build()
                }
            } catch (_: Exception) {
                // Conserva la sesión si la renovación falla por un problema temporal de red.
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var current: Response? = response
        var count = 1
        while (current?.priorResponse != null) {
            count++
            current = current.priorResponse
        }
        return count
    }

    private companion object {
        val refreshLock = Any()
    }
}
