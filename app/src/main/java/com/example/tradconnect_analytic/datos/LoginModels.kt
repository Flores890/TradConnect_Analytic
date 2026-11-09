package com.example.tradconnect_analytic.datos

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenData(
    val access: String?,
    val refresh: String?
)

data class LoginResponse(
    val tokens: TokenData?,
    val usuario: UsuarioData?
)

data class UsuarioData(
    val id: String?,
    @SerializedName("rol_nombre")
    val rolNombre: String?,
    @SerializedName("nombreusuario")
    val nombreUsuario: String?,
    @SerializedName("correoelectronico")
    val correoElectronico: String?
)

