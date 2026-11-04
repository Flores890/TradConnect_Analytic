package com.example.tradconnect_analytic.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tradconnect_analytic.datos.LoginRequest
import com.example.tradconnect_analytic.datos.RetrofitClient
import com.example.tradconnect_analytic.datos.TokenManager
import kotlinx.coroutines.launch

@Composable
fun PantallaLogin(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ProveedorAnalytics",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Acceso Administrativo y Proveedores",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico o usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Por favor, completa todos los campos"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.api.login(LoginRequest(email.trim(), password))

                            if (response.isSuccessful && response.body() != null) {
                                val resultado = response.body()!!
                                val rolNombre = resultado.usuario.nombre_rol?.lowercase() ?: ""
                                val rolTipo = resultado.usuario.rol?.lowercase() ?: ""

                                // Validación de roles permitidos (Admin o Proveedor)
                                val esValido = rolNombre.contains("admin") ||
                                        rolNombre.contains("administrador") ||
                                        rolNombre.contains("proveedor") ||
                                        rolNombre.contains("provider") ||
                                        rolTipo.contains("admin") ||
                                        rolTipo.contains("proveedor") ||
                                        rolTipo.contains("provider")

                                if (esValido) {
                                    // Guardar token de forma segura
                                    tokenManager.saveTokens(resultado.tokens.access, resultado.tokens.refresh)
                                    isLoading = false
                                    onLoginSuccess()
                                } else {
                                    isLoading = false
                                    errorMessage = "Acceso denegado: El rol '${resultado.usuario.nombre_rol}' no está autorizado."
                                }
                            } else {
                                isLoading = false
                                errorMessage = "Credenciales incorrectas o error en el servidor."
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "Error de conexión: ${e.localizedMessage}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Ingresar")
                }
            }
        }
    }
}