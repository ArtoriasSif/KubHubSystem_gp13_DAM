package com.example.kubhubsystem_gp13_dam.local.remote

import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductCreateDTO
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductResponseAnswerUpdateDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Define todos los endpoints relacionados con el módulo de Inventario.
 *
 * ✨ SINCRONIZADO CON InventarioController.java ✨
 *
 * Última revisión: 11 de noviembre de 2025
 *
 * @Importante: Esta interfaz asume que la BASE_URL en RetrofitClient
 * termina con una barra ("/"). Por eso, los endpoints aquí NO
 * comienzan con una barra.
 */
interface InventarioApiService {

    // --- Endpoints que devuelven el DTO para el Frontend (USO RECOMENDADO) ---
    /**
     * 🟢 /api/v1/inventario/find-all-inventories-active/
     * Obtiene la lista de todos los productos activos en el inventario.
     * (Equivalente a 'findAllActiveInventoryOrderedByName' en el Controller)
     */
    @GET("api/v1/inventario/find-all-inventories-active/")
    suspend fun getAllActiveInventories(): List<InventoryWithProductResponseAnswerUpdateDTO>

    /**
     * 🔵  /api/v1/inventario/create-inventory-with-product/
     * Crea un nuevo registro de inventario.
     * (Equivalente a 'save' en el Controller)
     */
    @POST("api/v1/inventario/create-inventory-with-product/")
    suspend fun createInventoryWithProduct(
        @Body inventarioRequest: InventoryWithProductCreateDTO
    ): InventoryWithProductCreateDTO

    /**
     * 🟡  /api/v1/inventario/update-inventory-with-product/
     * Actualiza un item de inventario existente.
     * (Equivalente a 'updateInventoryWithProduct' en el Controller)
     */
    @PUT("api/v1/inventario/update-inventory-with-product/")
    suspend fun updateInventoryWithProduct(
        @Body inventarioRequest: InventoryWithProductResponseAnswerUpdateDTO
    ): InventoryWithProductResponseAnswerUpdateDTO

    /**
     * 🔴   /api/v1/inventario/update-active-value-product-false/{id_inventario}
     * Realiza una eliminación lógica actualizando el estado 'activo' a false.
     * (Equivalente a 'updateActiveValueProductFalse' en el Controller)
     *
     * Se usa Response<Unit> para manejar respuestas vacías (HTTP 200/204 OK sin cuerpo).
     */
    @PUT("api/v1/inventario/update-active-value-product-false/{id_inventario}")
    suspend fun logicalDeleteInventoryItem(@Path("id_inventario") inventarioId: Int): Response<Unit>


    // --- Endpoints que devuelven la Entidad 'Inventario' (NO RECOMENDADOS PARA EL FRONTEND) ---
    // La app Android probablemente NO debería usar estos, ya que devuelven la Entidad
    // completa de la base de datos (Inventario.java), no un DTO limpio.

    /**
     * ⚠️ [GET] /api/v1/inventario/{id}
     * (Equivalente a 'findById' en el Controller)
     * ADVERTENCIA: Devuelve la entidad 'Inventario' completa, no un DTO.
     */
    // @GET("api/v1/inventario/{id}")
    // suspend fun findById(@Path("id") id: Int): Response<Any> // Descomentar si es necesario

    /**
     * ⚠️ [GET] /api/v1/inventario/id-activo/{id}/{activo}
     * (Equivalente a 'findByIdInventoryWithProductActive' en el Controller)
     * ADVERTENCIA: Devuelve la entidad 'Inventario' completa, no un DTO.
     */
    // @GET("api/v1/inventario/id-activo/{id}/{activo}")
    // suspend fun findByIdInventoryWithProductActive(
    //     @Path("id") id: Int,
    //     @Path("activo") activo: Boolean
    // ): Response<Any> // Descomentar si es necesario

    /**
     * ⚠️ [GET] /api/v1/inventario
     * (Equivalente a 'findAll' en el Controller)
     * ADVERTENCIA: Devuelve la entidad 'Inventario' completa, no un DTO.
     */
    // @GET("api/v1/inventario")
    // suspend fun findAll(): Response<Any> // Descomentar si es necesario

    /**
     * ⚠️ [GET] /api/v1/inventario/activo/{activo}
     * (Equivalente a 'findInventoriesWithProductsActive' en el Controller)
     * ADVERTENCIA: Devuelve la entidad 'Inventario' completa, no un DTO.
     */
    // @GET("api/v1/inventario/activo/{activo}")
    // suspend fun findInventoriesWithProductsActive(@Path("activo") activo: Boolean): Response<Any> // Descomentar

}