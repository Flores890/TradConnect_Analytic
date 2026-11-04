package com.example.tradconnect_analytic.datos

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenData(
    val access: String,
    val refresh: String
)

data class UsuarioData(
    val id: String?,
    val correo: String?,
    val nombre_usuario: String?,
    val rol: String?,
    val nombre_rol: String?
)

data class LoginResponse(
    val tokens: TokenData,
    val usuario: UsuarioData
)