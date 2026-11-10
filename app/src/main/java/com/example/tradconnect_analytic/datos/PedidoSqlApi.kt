package com.example.tradconnect_analytic.datos

import com.google.gson.annotations.SerializedName

data class PedidoSqlApi(
    val id: Int? = null,
    @SerializedName("restauranteid") val restauranteId: String? = null,
    @SerializedName("restaurante_nombre") val restauranteNombre: String? = null,
    @SerializedName("proveedorid") val proveedorId: String? = null,
    @SerializedName("metodopagoid") val metodoPagoId: Int? = null,
    @SerializedName("estadopedidoid") val estadoPedidoId: Int? = null,
    @SerializedName("monedaid") val monedaId: Int? = null,
    @SerializedName("totalneto") val totalNeto: String? = null,
    @SerializedName("fechapedido") val fechaPedido: String? = null,
    @SerializedName("estado_actual") val estadoActual: String? = null,
    @SerializedName("detalles") val detalles: List<DetallePedidoApi> = emptyList()
)

data class DetallePedidoApi(
    @SerializedName("productoid") val productoId: Int? = null,
    @SerializedName("producto_nombre") val productoNombre: String? = null,
    val cantidad: String? = null,
    // La API expone el subtotal de cada línea con la clave "subtotal".
    @SerializedName("subtotal") val subtotalLinea: String? = null
)

data class ProductoCatalogoApi(
    val id: Int? = null,
    @SerializedName("empresa_id") val empresaId: String? = null,
    @SerializedName("nombreproducto") val nombre: String? = null,
    @SerializedName("categoria_nombre") val categoria: String? = null,
    @SerializedName("unidadmedidaid") val unidadMedidaId: Int? = null,
    @SerializedName("unidad_nombre") val unidadNombre: String? = null,
    @SerializedName("imagenurl") val imagenUrl: String? = null
)

data class MonedaCatalogoApi(
    val id: Int? = null,
    @SerializedName("nombremoneda") val nombre: String? = null,
    @SerializedName("simbolo") val simbolo: String? = null
)

data class MetodoPagoApi(
    val id: Int? = null,
    val nombremetodo: String? = null
)
