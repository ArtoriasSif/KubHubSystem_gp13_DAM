package com.example.kubhubsystem_gp13_dam.local.remote
import com.example.kubhubsystem_gp13_dam.model.ProductoEntityDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

/**
 * ✅ API Retrofit para manejar los endpoints del microservicio de Producto
 * Basado en ProductoController de Spring Boot.
 */
interface ProductoApiService {

    /**
     * 🟢 Obtener todas las categorías (solo productos activos)
     * GET http://localhost:8080/api/v1/producto/find-categoria-name-product-active/
     */
    @GET("api/v1/producto/find-categoria-name-product-active/")
    suspend fun getCategoriasActivas(): List<String>

    /**
     * 🟢 Obtener todas las unidades de medida (solo productos activos)
     * GET http://localhost:8080/api/v1/producto/find-unidad-medida-product-active/
     */
    @GET("api/v1/producto/find-unidad-medida-product-active/")
    suspend fun getUnidadesMedidaActivas(): List<String>

    /**
     * 🟢 Obtener todos los productos (activos o no)
     * GET http://localhost:8080/api/v1/producto
     */
    @GET("api/v1/producto")
    suspend fun getAllProducts(): List<ProductoEntityDTO>

    /**
     * 🟢 Obtener productos según estado activo (TRUE/FALSE)
     * GET http://localhost:8080/api/v1/producto/all-value-active/{activo}
     */
    @GET("api/v1/producto/all-value-active/{activo}")
    suspend fun getProductsByActivo(@Path("activo") activo: Boolean): List<ProductoEntityDTO>

    /**
     * 🟢 Obtener producto por ID
     * GET http://localhost:8080/api/v1/producto/id/{id}
     */
    @GET("api/v1/producto/id/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductoEntityDTO

    /**
     * 🟢 Obtener producto activo por ID
     * GET http://localhost:8080/api/v1/producto/find-product-by-id-active/{id_producto}
     */
    @GET("api/v1/producto/find-product-by-id-active/{id_producto}")
    suspend fun getActiveProductById(@Path("id_producto") id: Int): ProductoEntityDTO
}
