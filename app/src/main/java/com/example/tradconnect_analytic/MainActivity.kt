package com.example.tradconnect_analytic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.tradconnect_analytic.datos.TokenManager
import com.example.tradconnect_analytic.pantallas.PantallaLogin
import com.example.tradconnect_analytic.ui.theme.TradConnect_AnalyticTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TradConnect_AnalyticTheme {
                val tokenManager = remember { TokenManager(this) }
                // Validar si ya existe un token guardado previamente
                var isLoggedIn by remember { mutableStateOf(tokenManager.getToken() != null) }

                if (isLoggedIn) {
                    // Si ya inició sesión, cargamos la estructura principal con la barra de navegación
                    TradConnectApp(
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