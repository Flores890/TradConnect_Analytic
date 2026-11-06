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
                text = "Inicio de Seción a TradConnect Analytic",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Area Administrativa y de Analicis",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico o nombre de usuario") },
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

                            // Desactivamos el indicador de carga al recibir respuesta
                            isLoading = false

                            if (response.isSuccessful && response.body() != null) {
                                val resultado = response.body()
                                val usuario = resultado?.usuario
                                val access = resultado?.tokens?.access
                                val refresh = resultado?.tokens?.refresh
                                val rolNombre = usuario?.rolNombre.orEmpty()

                                if (
                                    rolNombre.equals("Proveedor", ignoreCase = true) &&
                                    !access.isNullOrBlank() &&
                                    !refresh.isNullOrBlank()
                                ) {
                                    tokenManager.saveTokens(access, refresh)
                                    onLoginSuccess()
                                } else {
                                    errorMessage =
                                        "La respuesta no contiene tokens válidos o la cuenta no tiene el rol Proveedor."
                                }
                            } else {
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