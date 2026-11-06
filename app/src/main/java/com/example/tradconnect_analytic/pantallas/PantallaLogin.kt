package com.example.tradconnect_analytic.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lucide
import com.example.tradconnect_analytic.datos.LoginRequest
import com.example.tradconnect_analytic.datos.RetrofitClient
import com.example.tradconnect_analytic.datos.TokenManager
import kotlinx.coroutines.launch

private val FondoInicio = Color(0xFF0B1220)
private val FondoFinal = Color(0xFF142B43)
private val VerdeAcento = Color(0xFF35D6B0)
private val TextoSecundario = Color(0xFF64748B)

@Composable
fun PantallaLogin(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    val rolesPermitidos = remember {
        setOf("ADMIN", "ANALISTA", "PROVEEDOR", "GERENCIA")
    }

    var usuario by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var mostrarPassword by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    fun iniciarSesion() {
        if (usuario.isBlank() || password.isBlank()) {
            errorMessage = "Escribe tu usuario y contraseña para continuar."
            return
        }

        focusManager.clearFocus()
        errorMessage = null
        isLoading = true

        coroutineScope.launch {
            try {
                val response = RetrofitClient.api.login(
                    LoginRequest(
                        username = usuario.trim(),
                        password = password
                    )
                )

                if (!response.isSuccessful) {
                    errorMessage = when (response.code()) {
                        401 -> "Usuario o contraseña incorrectos."
                        else -> "No se pudo iniciar sesión. Código: ${response.code()}."
                    }
                    return@launch
                }

                val resultado = response.body()
                val access = resultado?.tokens?.access
                val refresh = resultado?.tokens?.refresh
                val rol = resultado?.usuario?.rolNombre
                    ?.trim()
                    ?.uppercase()
                    .orEmpty()

                if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
                    errorMessage = "La API respondió sin los tokens de acceso."
                } else if (rol !in rolesPermitidos) {
                    errorMessage = "El usuario no cuenta con credenciales adecuadas para acceder."
                } else {
                    tokenManager.saveTokens(access, refresh)
                    onLoginSuccess()
                 }

            } catch (_: Exception) {
                errorMessage = "No se pudo conectar. Comprueba la conexión e inténtalo de nuevo."
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(FondoInicio, Color(0xFF102238), FondoFinal)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = (-58).dp)
                .size(210.dp)
                .background(
                    color = VerdeAcento.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(VerdeAcento, Color(0xFF86F0D8))
                        ),
                        shape = RoundedCornerShape(19.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "T",
                    color = FondoInicio,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TradConnect Analytic",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tu comercio, en perspectiva.",
                color = Color(0xFFB8C7D9),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(30.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(26.dp)
                    ),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF9FBFD)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Inicia sesión",
                        color = Color(0xFF122238),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Continúa a tu panel de análisis.",
                        color = TextoSecundario,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = usuario,
                        onValueChange = {
                            usuario = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Correo o nombre de usuario") },
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFFFFFF),
                            unfocusedContainerColor = Color(0xFFF3F7FA),
                            disabledContainerColor = Color(0xFFE9EFF5),

                            disabledTextColor = Color(0xFF334155),
                            disabledBorderColor = Color(0xFF94A3B8),
                            disabledLabelColor = Color(0xFF52657A),
                            disabledTrailingIconColor = Color(0xFF52657A),
                            focusedTextColor = FondoInicio,

                            unfocusedTextColor = FondoInicio,
                            focusedBorderColor = Color(0xFF138F7A),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = Color(0xFF138F7A),
                            unfocusedLabelColor = TextoSecundario,
                            cursorColor = Color(0xFF138F7A)
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña") },
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        visualTransformation = if (mostrarPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { mostrarPassword = !mostrarPassword },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = if (mostrarPassword) {
                                        Lucide.EyeOff
                                    } else {
                                        Lucide.Eye
                                    },
                                    contentDescription = if (mostrarPassword) {
                                        "Ocultar contraseña"
                                    } else {
                                        "Mostrar contraseña"
                                    },
                                    tint = TextoSecundario
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { iniciarSesion() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FondoInicio,
                            unfocusedTextColor = FondoInicio,
                            focusedBorderColor = Color(0xFF138F7A),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = Color(0xFF138F7A),
                            unfocusedLabelColor = TextoSecundario,
                            cursorColor = Color(0xFF138F7A)
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage.orEmpty(),
                            color = Color(0xFFC2410C),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = { iniciarSesion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeAcento,
                            contentColor = FondoInicio,
                            disabledContainerColor = VerdeAcento.copy(alpha = 0.55f),
                            disabledContentColor = FondoInicio.copy(alpha = 0.7f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = FondoInicio,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Ingresando…",
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Ingresar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}