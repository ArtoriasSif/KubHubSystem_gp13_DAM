package com.example.kubhubsystem_gp13_dam.viewmodel

import com.example.kubhubsystem_gp13_dam.model.ProductoEntityDTO
import com.example.kubhubsystem_gp13_dam.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.ui.model.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ✅ PRUEBAS UNITARIAS PARA RecetaViewModel
 *
 * Valida:
 * - Transformaciones de StateFlow
 * - Estados UI (loading, success, error)
 * - Validaciones de datos
 * - Llamadas al repositorio (mocked)
 * - Filtros y búsqueda
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecetaViewModelTest {

    private lateinit var repository: RecetaRepository
    private lateinit var viewModel: RecetaViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Flows mockeados del repositorio
    private val mockRecetasFlow = MutableStateFlow<List<RecipeWithDetailsAnswerUpdateDTO>>(emptyList())
    private val mockProductosFlow = MutableStateFlow<List<ProductoEntityDTO>>(emptyList())
    private val mockUnidadesFlow = MutableStateFlow<List<String>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        repository = mockk(relaxed = true)

        // Configurar flows del repositorio
        every { repository.recetas } returns mockRecetasFlow
        every { repository.productosActivos } returns mockProductosFlow
        every { repository.unidadesMedida } returns mockUnidadesFlow

        // Configurar respuestas por defecto
        coEvery { repository.fetchAllActiveRecipes(any()) } returns Result.success(emptyList())
        coEvery { repository.fetchProductosActivos(any()) } returns Result.success(emptyList())
        coEvery { repository.fetchUnidadesMedida(any()) } returns Result.success(emptyList())
        coEvery { repository.updateCache(any()) } just Runs

        viewModel = RecetaViewModel(repository)
        // ✅ CRÍTICO: Avanzar el dispatcher después de crear el ViewModel
        // para que los flows derivados se inicialicen
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ============================================================
    // TESTS: Inicialización
    // ============================================================

    @Test
    fun `init - debe cargar recetas, productos y unidades al inicializar`() = runTest {
        // Then
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.fetchAllActiveRecipes(false) }
        coVerify(exactly = 1) { repository.fetchProductosActivos(false) }
        coVerify(exactly = 1) { repository.fetchUnidadesMedida(false) }
    }

    // ============================================================
    // TESTS: loadRecipes()
    // ============================================================

    @Test
    fun `loadRecipes - debe activar isLoading durante la carga`() = runTest {
        // Given
        val mockRecetas = listOf(
            createMockRecipe(1, "Pizza", EstadoRecetaType.ACTIVO)
        )
        coEvery { repository.fetchAllActiveRecipes(true) } coAnswers {
            delay(100)
            Result.success(mockRecetas)
        }
        coEvery { repository.updateCache(any()) } just Runs

        // When
        viewModel.loadRecipes(forceRefresh = true)

        // Avanza solo un poco para capturar el estado de loading
        advanceTimeBy(1)
        assertTrue(viewModel.isLoading.value)

        // Ahora deja terminar
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadRecipes - debe actualizar cache cuando la carga es exitosa`() = runTest {
        // Given
        val mockRecetas = listOf(
            createMockRecipe(1, "Pizza", EstadoRecetaType.ACTIVO),
            createMockRecipe(2, "Pasta", EstadoRecetaType.ACTIVO)
        )
        coEvery { repository.fetchAllActiveRecipes(true) } returns Result.success(mockRecetas)
        coEvery { repository.updateCache(any()) } just Runs

        // When
        viewModel.loadRecipes(forceRefresh = true)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.errorMessage.first())
        coVerify(exactly = 1) { repository.updateCache(mockRecetas) }
    }



    // ============================================================
    // TESTS: createRecipeWithDetails()
    // ============================================================

    @Test
    fun `createRecipeWithDetails - debe validar nombre requerido`() = runTest {
        // When
        viewModel.createRecipeWithDetails(
            nombreReceta = "",
            descripcionReceta = "",
            ingredientes = listOf(createMockRecipeItem()),
            instrucciones = "",
            estadoReceta = EstadoRecetaType.ACTIVO
        )
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains("nombre") == true)
        assertFalse(viewModel.isSaving.value)
    }

    @Test
    fun `createRecipeWithDetails - debe validar ingredientes requeridos`() = runTest {
        // When
        viewModel.createRecipeWithDetails(
            nombreReceta = "Pizza",
            descripcionReceta = "",
            ingredientes = emptyList(),
            instrucciones = "",
            estadoReceta = EstadoRecetaType.ACTIVO
        )
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains("ingrediente") == true)
        assertFalse(viewModel.isSaving.value)
    }

    @Test
    fun `createRecipeWithDetails - debe crear receta exitosamente`() = runTest {
        // Given
        val mockDto = RecipeWithDetailsCreateDTO(
            nombreReceta = "Pizza",
            descripcionReceta = null,
            listaItems = listOf(createMockRecipeItem()),
            instrucciones = null,
            estadoReceta = EstadoRecetaType.ACTIVO
        )
        val mockRecetas = listOf(createMockRecipe(1, "Pizza", EstadoRecetaType.ACTIVO))

        coEvery { repository.createRecipeWithDetails(any()) } returns Result.success(mockDto)
        coEvery { repository.fetchAllActiveRecipes(true) } returns Result.success(mockRecetas)
        coEvery { repository.updateCache(any()) } just Runs

        // When
        viewModel.createRecipeWithDetails(
            nombreReceta = "Pizza",
            descripcionReceta = "",
            ingredientes = listOf(createMockRecipeItem()),
            instrucciones = "",
            estadoReceta = EstadoRecetaType.ACTIVO
        )
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.successMessage.value?.contains("Pizza") == true)
        assertFalse(viewModel.isSaving.value)
        coVerify(exactly = 1) { repository.createRecipeWithDetails(any()) }
    }

    @Test
    fun `createRecipeWithDetails - debe activar isSaving durante la creación`() = runTest {
        // Given
        val mockDto = RecipeWithDetailsCreateDTO(
            nombreReceta = "Pizza",
            descripcionReceta = null,
            listaItems = listOf(createMockRecipeItem()),
            instrucciones = null,
            estadoReceta = EstadoRecetaType.ACTIVO
        )

        coEvery { repository.createRecipeWithDetails(any()) } coAnswers {
            delay(100)
            Result.success(mockDto)
        }
        coEvery { repository.fetchAllActiveRecipes(true) } returns Result.success(emptyList())
        coEvery { repository.updateCache(any()) } just Runs

        // When
        viewModel.createRecipeWithDetails(
            nombreReceta = "Pizza",
            descripcionReceta = "",
            ingredientes = listOf(createMockRecipeItem()),
            instrucciones = "",
            estadoReceta = EstadoRecetaType.ACTIVO
        )

        // Avanza solo un poco para capturar el estado de saving
        advanceTimeBy(1)
        assertTrue(viewModel.isSaving.value)

        // Ahora deja terminar
        advanceUntilIdle()
        assertFalse(viewModel.isSaving.value)
    }

    @Test
    fun `createRecipeWithDetails - debe manejar error en creación`() = runTest {
        // Given
        coEvery { repository.createRecipeWithDetails(any()) } returns
                Result.failure(Exception("Error al crear"))

        // When
        viewModel.createRecipeWithDetails(
            nombreReceta = "Pizza",
            descripcionReceta = "",
            ingredientes = listOf(createMockRecipeItem()),
            instrucciones = "",
            estadoReceta = EstadoRecetaType.ACTIVO
        )
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains("Error") == true)
        assertFalse(viewModel.isSaving.value)
    }

    // ============================================================
    // TESTS: updateRecipeWithDetails()
    // ============================================================

    @Test
    fun `updateRecipeWithDetails - debe validar nombre requerido`() = runTest {
        // Given
        val dto = RecipeWithDetailsAnswerUpdateDTO(
            idReceta = 1,
            nombreReceta = "",
            descripcionReceta = null,
            listaItems = listOf(createMockRecipeItem()),
            instrucciones = null,
            estadoReceta = EstadoRecetaType.ACTIVO,
            cambioReceta = false,
            cambioDetalles = false
        )

        // When
        viewModel.updateRecipeWithDetails(dto)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains("nombre") == true)
        assertFalse(viewModel.isSaving.value)
    }

    @Test
    fun `updateRecipeWithDetails - debe validar ingredientes requeridos`() = runTest {
        // Given
        val dto = RecipeWithDetailsAnswerUpdateDTO(
            idReceta = 1,
            nombreReceta = "Pizza",
            descripcionReceta = null,
            listaItems = emptyList(),
            instrucciones = null,
            estadoReceta = EstadoRecetaType.ACTIVO,
            cambioReceta = false,
            cambioDetalles = false
        )

        // When
        viewModel.updateRecipeWithDetails(dto)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains("ingrediente") == true)
        assertFalse(viewModel.isSaving.value)
    }

    @Test
    fun `updateRecipeWithDetails - debe actualizar receta exitosamente`() = runTest {
        // Given
        val dto = RecipeWithDetailsAnswerUpdateDTO(
            idReceta = 1,
            nombreReceta = "Pizza",
            descripcionReceta = null,
            listaItems = listOf(createMockRecipeItem()),
            instrucciones = null,
            estadoReceta = EstadoRecetaType.ACTIVO,
            cambioReceta = false,
            cambioDetalles = false
        )
        val mockRecetas = listOf(createMockRecipe(1, "Pizza", EstadoRecetaType.ACTIVO))

        coEvery { repository.updateRecipeWithDetails(any()) } returns Result.success(dto)
        coEvery { repository.fetchAllActiveRecipes(true) } returns Result.success(mockRecetas)
        coEvery { repository.updateCache(any()) } just Runs

        // When
        viewModel.updateRecipeWithDetails(dto)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.successMessage.value?.contains("Pizza") == true)
        assertFalse(viewModel.isSaving.value)
        coVerify(exactly = 1) { repository.updateRecipeWithDetails(any()) }
    }

    @Test
    fun `updateRecipeWithDetails - debe manejar error en actualización`() = runTest {
        // Given
        val dto = RecipeWithDetailsAnswerUpdateDTO(
            idReceta = 1,
            nombreReceta = "Pizza",
            descripcionReceta = null,
            listaItems = listOf(createMockRecipeItem()),
            instrucciones = null,
            estadoReceta = EstadoRecetaType.ACTIVO,
            cambioReceta = false,
            cambioDetalles = false
        )
        coEvery { repository.updateRecipeWithDetails(any()) } returns
                Result.failure(Exception("Error al actualizar"))

        // When
        viewModel.updateRecipeWithDetails(dto)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains("Error") == true)
        assertFalse(viewModel.isSaving.value)
    }

    // ============================================================
    // TESTS: updateChangingStatus()
    // ============================================================

    @Test
    fun `updateChangingStatus - debe cambiar estado exitosamente`() = runTest {
        // Given
        val idReceta = 1
        val mockRecetas = listOf(createMockRecipe(1, "Pizza", EstadoRecetaType.INACTIVO))

        coEvery { repository.updateChangingStatusRecipeWith(idReceta) } returns true
        coEvery { repository.fetchAllActiveRecipes(true) } returns Result.success(mockRecetas)
        coEvery { repository.updateCache(any()) } just Runs

        // When
        viewModel.updateChangingStatus(idReceta)
        advanceUntilIdle()

        // Then
        assertEquals(true, viewModel.statusChangeResult.value)
        assertTrue(viewModel.successMessage.value?.contains("actualizado") == true)
        assertFalse(viewModel.isLoading.value)
        coVerify(exactly = 1) { repository.updateChangingStatusRecipeWith(idReceta) }
    }

    @Test
    fun `updateChangingStatus - debe manejar error al cambiar estado`() = runTest {
        // Given
        val idReceta = 1
        coEvery { repository.updateChangingStatusRecipeWith(idReceta) } returns false

        // When
        viewModel.updateChangingStatus(idReceta)
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.statusChangeResult.value)
        assertTrue(viewModel.errorMessage.value?.contains("No se pudo") == true)
        assertFalse(viewModel.isLoading.value)
    }

    // ============================================================
    // TESTS: deactivateRecipe()
    // ============================================================

    @Test
    fun `deactivateRecipe - debe desactivar receta exitosamente`() = runTest {
        // Given
        val idReceta = 1
        coEvery { repository.deactivateRecipe(idReceta) } returns Result.success(Unit)
        coEvery { repository.fetchAllActiveRecipes(true) } returns Result.success(emptyList())
        coEvery { repository.updateCache(any()) } just Runs

        // When
        viewModel.deactivateRecipe(idReceta, "Pizza")
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.successMessage.value?.contains("ELIMINADA") == true)
        assertFalse(viewModel.isLoading.value)
        coVerify(exactly = 1) { repository.deactivateRecipe(idReceta) }
        coVerify(exactly = 1) { repository.fetchAllActiveRecipes(true) }
    }

    @Test
    fun `deactivateRecipe - debe manejar error al desactivar`() = runTest {
        // Given
        val idReceta = 1
        coEvery { repository.deactivateRecipe(idReceta) } returns
                Result.failure(Exception("Error al desactivar"))

        // When
        viewModel.deactivateRecipe(idReceta, "Pizza")
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains("Error al desactivar") == true)
        assertFalse(viewModel.isLoading.value)
    }

    // ============================================================
    // TESTS: Productos y Unidades
    // ============================================================

    @Test
    fun `loadProductosActivos - debe cargar productos del repositorio`() = runTest {
        // Given
        val mockProductos = listOf(
            createMockProducto(1, "Harina"),
            createMockProducto(2, "Tomate")
        )
        coEvery { repository.fetchProductosActivos(true) } returns Result.success(mockProductos)

        // When
        viewModel.loadProductosActivos(forceRefresh = true)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { repository.fetchProductosActivos(true) }
    }

    @Test
    fun `loadUnidadesMedida - debe cargar unidades del repositorio`() = runTest {
        // Given
        val mockUnidades = listOf("kg", "gr", "litros")
        coEvery { repository.fetchUnidadesMedida(true) } returns Result.success(mockUnidades)

        // When
        viewModel.loadUnidadesMedida(forceRefresh = true)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { repository.fetchUnidadesMedida(true) }
    }

    // ============================================================
    // TESTS: Categorías y Estados
    // ============================================================





    // ============================================================
    // TESTS: Utilidades
    // ============================================================

    @Test
    fun `refresh - debe refrescar recetas, productos y unidades`() = runTest {
        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { repository.fetchAllActiveRecipes(true) }
        coVerify(exactly = 1) { repository.fetchProductosActivos(true) }
        coVerify(exactly = 1) { repository.fetchUnidadesMedida(true) }
    }

    @Test
    fun `clearError - debe limpiar mensaje de error`() = runTest {
        // Given
        coEvery { repository.fetchAllActiveRecipes(true) } returns
                Result.failure(Exception("Error"))
        viewModel.loadRecipes(forceRefresh = true)
        advanceUntilIdle()

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `clearSuccess - debe limpiar mensaje de éxito`() = runTest {
        // Given
        val mockDto = RecipeWithDetailsCreateDTO(
            nombreReceta = "Pizza",
            descripcionReceta = null,
            listaItems = listOf(createMockRecipeItem()),
            instrucciones = null,
            estadoReceta = EstadoRecetaType.ACTIVO
        )
        val mockRecetas = listOf(createMockRecipe(1, "Pizza", EstadoRecetaType.ACTIVO))

        coEvery { repository.createRecipeWithDetails(any()) } returns Result.success(mockDto)
        coEvery { repository.fetchAllActiveRecipes(true) } returns Result.success(mockRecetas)
        coEvery { repository.updateCache(any()) } just Runs

        viewModel.createRecipeWithDetails(
            nombreReceta = "Pizza",
            descripcionReceta = "",
            ingredientes = listOf(createMockRecipeItem()),
            instrucciones = "",
            estadoReceta = EstadoRecetaType.ACTIVO
        )
        advanceUntilIdle()

        // When
        viewModel.clearSuccess()

        // Then
        assertNull(viewModel.successMessage.value)
    }

    @Test
    fun `getRecipe - debe obtener receta del cache del repositorio`() = runTest {
        // Given
        val mockRecipe = createMockRecipe(1, "Pizza", EstadoRecetaType.ACTIVO)
        every { repository.getRecipeFromCache(1) } returns mockRecipe

        // When
        val receta = viewModel.getRecipe(1)

        // Then
        assertNotNull(receta)
        assertEquals("Pizza", receta?.nombreReceta)
        verify(exactly = 1) { repository.getRecipeFromCache(1) }
    }

    // ============================================================
    // Helpers
    // ============================================================

    private fun createMockRecipe(
        id: Int,
        nombre: String,
        estado: EstadoRecetaType,
        descripcion: String? = null
    ): RecipeWithDetailsAnswerUpdateDTO {
        return RecipeWithDetailsAnswerUpdateDTO(
            idReceta = id,
            nombreReceta = nombre,
            descripcionReceta = descripcion,
            listaItems = listOf(createMockRecipeItem()),
            instrucciones = "Instrucciones",
            estadoReceta = estado,
            cambioReceta = false,
            cambioDetalles = false
        )
    }

    private fun createMockRecipeItem(): RecipeItemDTO {
        return RecipeItemDTO(
            idProducto = 10,
            nombreProducto = "Harina",
            cantUnidadMedida = 500.0,
            unidadMedida = "gr",
            activo = true
        )
    }

    private fun createMockProducto(id: Int, nombre: String): ProductoEntityDTO {
        return ProductoEntityDTO(
            idProducto = id,
            nombreProducto = nombre,
            nombreCategoria = "Granos",
            unidadMedida = "kg",
            activo = true,
            codProducto = "PROD$id",
            descripcionProducto = null,
            fotoProducto = null
        )
    }
}