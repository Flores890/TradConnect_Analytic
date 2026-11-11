package com.example.tradconnect_analytic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.tradconnect_analytic.datos.TokenManager
import com.example.tradconnect_analytic.datos.RetrofitClient
import com.example.tradconnect_analytic.pantallas.PantallaLogin
import com.example.tradconnect_analytic.ui.theme.TradConnect_AnalyticTheme
import java.util.Locale

/** Inicializa la API y decide si muestra el acceso o la navegación principal. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.initialize(applicationContext)
        val savedTokenManager = TokenManager(this)
        val allowedRoles = setOf("PROVEEDOR")
        val savedRole = savedTokenManager.getRole()?.trim()?.uppercase(Locale.ROOT)
        if (!savedTokenManager.getToken().isNullOrBlank() && savedRole !in allowedRoles) {
            savedTokenManager.clear()
        }
        setContent {
            TradConnect_AnalyticTheme {
                val tokenManager = remember { TokenManager(this) }
                var isLoggedIn by remember { mutableStateOf(!tokenManager.getToken().isNullOrBlank()) }

                if (isLoggedIn) {
                    // Abre la navegación principal cuando existe una sesión válida.
                    TradConnectApp(
                        role = tokenManager.getRole().orEmpty(),
                        onLogout = {
                            tokenManager.clear()
                            isLoggedIn = false
                        }
                    )
                } else {
                    // Sin sesión activa, solicita las credenciales del proveedor.
                    PantallaLogin(
                        onLoginSuccess = {
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }
}
