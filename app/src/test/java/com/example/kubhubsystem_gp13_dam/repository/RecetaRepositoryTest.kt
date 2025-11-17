package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.remote.ProductoApiService
import com.example.kubhubsystem_gp13_dam.local.remote.RecetaApiService
import com.example.kubhubsystem_gp13_dam.model.ProductoEntityDTO
import com.example.kubhubsystem_gp13_dam.ui.model.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * ✅ PRUEBAS UNITARIAS PARA RecetaRepository
 *
 * Valida:
 * - Llamadas correctas al API
 * - Manejo de cache
 * - Transformación de datos
 * - Manejo de excepciones
 * - Estados de loading y error
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecetaRepositoryTest {

    private lateinit var recetaApiService: RecetaApiService
    private lateinit var productoApiService: ProductoApiService
    private lateinit var repository: RecetaRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        recetaApiService = mockk()
        productoApiService = mockk()
        repository = RecetaRepository(recetaApiService, productoApiService)

        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ============================================================
    // TESTS: fetchAllActiveRecipes()
    // ============================================================

    @Test
    fun `fetchAllActiveRecipes - debe retornar recetas desde API y actualizar cache`() = runTest {
        // Given
        val mockRecetas = listOf(
            RecipeWithDetailsAnswerUpdateDTO(
                idReceta = 1,
                nombreReceta = "Pizza",
                descripcionReceta = "Pizza italiana",
                listaItems = emptyList(),
                instrucciones = "Hornear",
                estadoReceta = EstadoRecetaType.ACTIVO,
                cambioReceta = false,
                cambioDetalles = false
            ),
            RecipeWithDetailsAnswerUpdateDTO(
                idReceta = 2,
                nombreReceta = "Pasta",
                descripcionReceta = "Pasta carbonara",
                listaItems = emptyList(),
                instrucciones = "Cocinar",
                estadoReceta = EstadoRecetaType.ACTIVO,
                cambioReceta = false,
                cambioDetalles = false
            )
        )

        coEvery { recetaApiService.findAllRecipeWithDetailsActive() } returns mockRecetas

        // When
        val result = repository.fetchAllActiveRecipes(forceRefresh = true)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Pizza", result.getOrNull()?.first()?.nombreReceta)

        coVerify(exactly = 1) { recetaApiService.findAllRecipeWithDetailsActive() }
    }

    @Test
    fun `fetchAllActiveRecipes - debe manejar HttpException correctamente`() = runTest {
        // Given
        val httpException = HttpException(
            Response.error<Any>(404, "Not Found".toResponseBody())
        )
        coEvery { recetaApiService.findAllRecipeWithDetailsActive() } throws httpException

        // When
        val result = repository.fetchAllActiveRecipes(forceRefresh = true)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `fetchAllActiveRecipes - debe manejar IOException correctamente`() = runTest {
        // Given
        coEvery { recetaApiService.findAllRecipeWithDetailsActive() } throws IOException("Network error")

        // When
        val result = repository.fetchAllActiveRecipes(forceRefresh = true)

        // Then
        assertTrue(result.isFailure)
    }

    // ============================================================
    // TESTS: createRecipeWithDetails()
    // ============================================================

    @Test
    fun `createRecipeWithDetails - debe crear receta y refrescar cache`() = runTest {
        // Given
        val dtoCreate = RecipeWithDetailsCreateDTO(
            nombreReceta = "Nueva Receta",
            descripcionReceta = "Descripción",
            listaItems = listOf(
                RecipeItemDTO(
                    idProducto = 1,
                    nombreProducto = "Harina",
                    cantUnidadMedida = 500.0,
                    unidadMedida = "gr",
                    activo = true
                )
            ),
            instrucciones = "Instrucciones",
            estadoReceta = EstadoRecetaType.ACTIVO
        )

        val mockRecetaCreada = dtoCreate
        val mockRecetasActualizadas = listOf(
            RecipeWithDetailsAnswerUpdateDTO(
                idReceta = 1,
                nombreReceta = "Nueva Receta",
                descripcionReceta = "Descripción",
                listaItems = dtoCreate.listaItems,
                instrucciones = "Instrucciones",
                estadoReceta = EstadoRecetaType.ACTIVO,
                cambioReceta = false,
                cambioDetalles = false
            )
        )

        coEvery { recetaApiService.createRecipeWithDetails(dtoCreate) } returns mockRecetaCreada
        coEvery { recetaApiService.findAllRecipeWithDetailsActive() } returns mockRecetasActualizadas

        // When
        val result = repository.createRecipeWithDetails(dtoCreate)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Nueva Receta", result.getOrNull()?.nombreReceta)

        coVerify(exactly = 1) { recetaApiService.createRecipeWithDetails(dtoCreate) }
        coVerify(exactly = 1) { recetaApiService.findAllRecipeWithDetailsActive() }
    }

    @Test
    fun `createRecipeWithDetails - debe manejar error al crear`() = runTest {
        // Given
        val dtoCreate = RecipeWithDetailsCreateDTO(
            nombreReceta = "Receta Inválida",
            descripcionReceta = null,
            listaItems = emptyList(),
            instrucciones = null,
            estadoReceta = EstadoRecetaType.ACTIVO
        )

        coEvery { recetaApiService.createRecipeWithDetails(dtoCreate) } throws
                HttpException(Response.error<Any>(400, "Bad Request".toResponseBody()))

        // When
        val result = repository.createRecipeWithDetails(dtoCreate)

        // Then
        assertTrue(result.isFailure)
    }

    // ============================================================
    // TESTS: updateRecipeWithDetails()
    // ============================================================

    @Test
    fun `updateRecipeWithDetails - debe actualizar receta exitosamente`() = runTest {
        // Given
        val dtoUpdate = RecipeWithDetailsAnswerUpdateDTO(
            idReceta = 1,
            nombreReceta = "Receta Actualizada",
            descripcionReceta = "Nueva descripción",
            listaItems = listOf(
                RecipeItemDTO(
                    idProducto = 1,
                    nombreProducto = "Harina",
                    cantUnidadMedida = 600.0,
                    unidadMedida = "gr",
                    activo = true
                )
            ),
            instrucciones = "Nuevas instrucciones",
            estadoReceta = EstadoRecetaType.ACTIVO,
            cambioReceta = false,
            cambioDetalles = false
        )

        val mockRecetasActualizadas = listOf(dtoUpdate)

        coEvery { recetaApiService.updateRecipeWithDetails(dtoUpdate) } returns dtoUpdate
        coEvery { recetaApiService.findAllRecipeWithDetailsActive() } returns mockRecetasActualizadas

        // When
        val result = repository.updateRecipeWithDetails(dtoUpdate)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Receta Actualizada", result.getOrNull()?.nombreReceta)
        assertEquals(600.0, result.getOrNull()?.listaItems?.first()?.cantUnidadMedida ?: 0.0, 0.01)

        coVerify(exactly = 1) { recetaApiService.updateRecipeWithDetails(dtoUpdate) }
        coVerify(exactly = 1) { recetaApiService.findAllRecipeWithDetailsActive() }
    }

    @Test
    fun `updateRecipeWithDetails - debe manejar error 404 al actualizar`() = runTest {
        // Given
        val dtoUpdate = RecipeWithDetailsAnswerUpdateDTO(
            idReceta = 9999,
            nombreReceta = "Receta Inexistente",
            descripcionReceta = null,
            listaItems = emptyList(),
            instrucciones = null,
            estadoReceta = EstadoRecetaType.ACTIVO,
            cambioReceta = false,
            cambioDetalles = false
        )

        coEvery { recetaApiService.updateRecipeWithDetails(dtoUpdate) } throws
                HttpException(Response.error<Any>(404, "Not Found".toResponseBody()))

        // When
        val result = repository.updateRecipeWithDetails(dtoUpdate)

        // Then
        assertTrue(result.isFailure)
    }

    // ============================================================
    // TESTS: updateChangingStatusRecipeWith()
    // ============================================================

    @Test
    fun `updateChangingStatusRecipeWith - debe cambiar estado exitosamente`() = runTest {
        // Given
        val idReceta = 1
        val mockResponse = Response.success<okhttp3.ResponseBody>(
            "".toResponseBody()
        )

        coEvery { recetaApiService.updateChangingStatusRecipeWith(idReceta) } returns mockResponse

        // When
        val result = repository.updateChangingStatusRecipeWith(idReceta)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { recetaApiService.updateChangingStatusRecipeWith(idReceta) }
    }

    @Test
    fun `updateChangingStatusRecipeWith - debe retornar false en caso de error`() = runTest {
        // Given
        val idReceta = 1

        coEvery { recetaApiService.updateChangingStatusRecipeWith(idReceta) } throws
                IOException("Network error")

        // When
        val result = repository.updateChangingStatusRecipeWith(idReceta)

        // Then
        assertFalse(result)
    }

    // ============================================================
    // TESTS: deactivateRecipe()
    // ============================================================

    @Test
    fun `deactivateRecipe - debe desactivar receta exitosamente`() = runTest {
        // Given
        val idReceta = 1
        val mockResponse = Response.success<Unit>(Unit)
        coEvery { recetaApiService.updateStatusActiveFalseRecipe(idReceta) } returns mockResponse

        // When
        val result = repository.deactivateRecipe(idReceta)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { recetaApiService.updateStatusActiveFalseRecipe(idReceta) }
    }

    @Test
    fun `deactivateRecipe - debe manejar error 404`() = runTest {
        // Given
        val idReceta = 9999
        val mockResponse = Response.error<Unit>(
            404,
            "Not Found".toResponseBody()
        )

        coEvery { recetaApiService.updateStatusActiveFalseRecipe(idReceta) } returns mockResponse

        // When
        val result = repository.deactivateRecipe(idReceta)

        // Then
        assertTrue(result.isFailure)
    }

    // ============================================================
    // TESTS: fetchProductosActivos()
    // ============================================================

    @Test
    fun `fetchProductosActivos - debe retornar productos ordenados alfabéticamente`() = runTest {
        // Given
        val mockProductos = listOf(
            ProductoEntityDTO(
                idProducto = 2,
                nombreProducto = "Tomate",
                nombreCategoria = "Vegetales",
                unidadMedida = "kg",
                activo = true
            ),
            ProductoEntityDTO(
                idProducto = 1,
                nombreProducto = "Harina",
                nombreCategoria = "Granos",
                unidadMedida = "kg",
                activo = true
            )
        )

        coEvery { productoApiService.getProductsByActivo(true) } returns mockProductos

        // When
        val result = repository.fetchProductosActivos(forceRefresh = true)

        // Then
        assertTrue(result.isSuccess)
        val productos = result.getOrNull()!!
        assertEquals(2, productos.size)
        assertEquals("Harina", productos.first().nombreProducto) // Ordenado alfabéticamente

        coVerify(exactly = 1) { productoApiService.getProductsByActivo(true) }
    }
}