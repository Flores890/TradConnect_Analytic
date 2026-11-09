package com.example.tradconnect_analytic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.tradconnect_analytic.datos.TokenManager
import com.example.tradconnect_analytic.pantallas.PantallaLogin
import com.example.tradconnect_analytic.ui.theme.TradConnect_AnalyticTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedTokenManager = TokenManager(this)
        val allowedRoles = setOf("ADMIN", "ADMINISTRADOR", "PROVEEDOR")
        val savedRole = savedTokenManager.getRole()?.trim()?.uppercase(Locale.ROOT)
        if (!savedTokenManager.getToken().isNullOrBlank() && savedRole !in allowedRoles) {
            savedTokenManager.clear()
        }
        setContent {
            TradConnect_AnalyticTheme {
                val tokenManager = remember { TokenManager(this) }
                var isLoggedIn by remember { mutableStateOf(!tokenManager.getToken().isNullOrBlank()) }

                if (isLoggedIn) {
                    // Si ya inició sesión, cargamos la estructura principal con la barra de navegación
                    TradConnectApp(
                        role = tokenManager.getRole().orEmpty(),
                        onLogout = {
                            tokenManager.clear()
                            isLoggedIn = false
                        }
                    )
                } else {
                    // Si no, mostramos la pantalla de Login
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
