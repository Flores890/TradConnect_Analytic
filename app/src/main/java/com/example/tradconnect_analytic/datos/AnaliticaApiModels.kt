package com.example.tradconnect_analytic.datos

import com.google.gson.annotations.SerializedName

data class PaginaApi<T>(
    val results: List<T> = emptyList(),
    val offset: Int = 0
)

data class AnaliticaProveedorApi(
    val ventas: List<VentaMesApi> = emptyList(),
    val estados: List<EstadoPedidoApi> = emptyList(),
    val productos: List<ProductoVendidoApi> = emptyList(),
    val categorias: List<CategoriaVentaApi> = emptyList(),
    val clientes: List<ClienteFrecuenteApi> = emptyList(),
    val feedback: FeedbackApi? = null,
    @SerializedName("moneda")
    val monedaId: Int? = null
)

data class MiDataApi(
    val empresa: EmpresaSesionApi? = null
)

data class EmpresaSesionApi(
    val id: String? = null
)

data class VentaMesApi(
    @SerializedName("fecha__fecha")
    val fecha: String? = null,
    @SerializedName("total_ventas")
    val totalVentas: String? = null,
    @SerializedName("total_pedidos")
    val totalPedidos: Int = 0,
    @SerializedName("cantidad_items")
    val cantidadItems: String? = null
)

data class EstadoPedidoApi(
    @SerializedName("estado_pedido__estado_nombre")
    val estado: String? = null,
    @SerializedName("cantidad_pedidos")
    val cantidad: Int = 0
)

data class ProductoVendidoApi(
    @SerializedName("producto__nombre_producto")
    val nombre: String? = null,
    @SerializedName("total_vendido")
    val cantidad: String? = null,
    @SerializedName("total_ingreso")
    val ingreso: String? = null
)

data class CategoriaVentaApi(
    @SerializedName("producto__categoria")
    val categoria: String? = null,
    @SerializedName("total_ingreso")
    val ingreso: String? = null,
    @SerializedName("total_cantidad")
    val cantidad: String? = null
)

data class ClienteFrecuenteApi(
    @SerializedName("empresa__razon_social")
    val empresa: String? = null,
    @SerializedName("total_comprado")
    val totalComprado: String? = null,
    @SerializedName("total_pedidos")
    val pedidos: Int = 0
)

data class FeedbackApi(
    val disponible: Boolean = false,
    val evaluaciones: Int = 0,
    val puntuacion: Double? = null,
    val reclamos: Int = 0,
    val pendientes: Int = 0,
    val resueltos: Int = 0
)

data class EvaluacionApi(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("proveedor_empresa_id")
    val proveedorEmpresaId: String? = null,
    @SerializedName("cliente_nombre")
    val cliente: String? = null,
    val puntuacion: Double? = null,
    val calidad: Int? = null,
    @SerializedName("tiempo_entrega")
    val tiempoEntrega: Int? = null,
    val precio: Int? = null,
    val atencion: Int? = null,
    val comentario: String? = null,
    val fecha: String? = null
)

data class IncidenciaApi(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("proveedor_empresa_id")
    val proveedorEmpresaId: String? = null,
    @SerializedName("cliente_nombre")
    val cliente: String? = null,
    @SerializedName("tipo_incidencia")
    val tipo: String? = null,
    @SerializedName("tipo_reclamo")
    val reclamo: String? = null,
    val estado: String? = null,
    val fecha: String? = null,
    @SerializedName("fecha_respuesta")
    val fechaRespuesta: String? = null,
    @SerializedName("respuesta_proveedor")
    val respuestaProveedor: String? = null
)


