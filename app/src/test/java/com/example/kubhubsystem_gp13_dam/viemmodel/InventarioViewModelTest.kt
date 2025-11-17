package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductCreateDTO
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductResponseAnswerUpdateDTO
import com.example.kubhubsystem_gp13_dam.repository.InventarioRepository
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
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
 * ✅ PRUEBAS UNITARIAS PARA InventarioViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InventarioViewModelTest {

    private lateinit var inventarioRepository: InventarioRepository
    private lateinit var productoRepository: ProductoRepository
    private lateinit var viewModel: InventarioViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    // Flows mockeados
    private val mockInventariosFlow = MutableStateFlow<List<InventoryWithProductResponseAnswerUpdateDTO>>(emptyList())
    private val mockCategoriasFlow = MutableStateFlow<List<String>>(emptyList())
    private val mockUnidadesFlow = MutableStateFlow<List<String>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        inventarioRepository = mockk(relaxed = true)
        productoRepository = mockk(relaxed = true)

        // Configurar flows
        every { inventarioRepository.inventarios } returns mockInventariosFlow
        every { productoRepository.categorias } returns mockCategoriasFlow
        every { productoRepository.unidadesMedida } returns mockUnidadesFlow

        // Configurar respuestas por defecto
        coEvery { inventarioRepository.fetchAllActiveInventories(any()) } returns Result.success(emptyList())
        coEvery { productoRepository.fetchCategoriasActivas(any()) } returns Result.success(emptyList())
        coEvery { productoRepository.fetchUnidadesMedidaActivas(any()) } returns Result.success(emptyList())

        viewModel = InventarioViewModel(inventarioRepository, productoRepository)
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
    fun `init - debe cargar inventarios, categorias y unidades al inicializar`() = runTest {
        coVerify(exactly = 1) { inventarioRepository.fetchAllActiveInventories(false) }
        coVerify(exactly = 1) { productoRepository.fetchCategoriasActivas(false) }
        coVerify(exactly = 1) { productoRepository.fetchUnidadesMedidaActivas(false) }
    }

    // ============================================================
    // TESTS: loadInventories()
    // ============================================================

    @Test
    fun `loadInventories - debe activar isLoading durante la carga`() = runTest {
        val testScheduler = TestCoroutineScheduler()
        val testDispatcher2 = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher2)

        val mockInventarios = listOf(createMockInventory(1, "Harina", "DISPONIBLE"))
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } coAnswers {
            delay(100)
            Result.success(mockInventarios)
        }

        val vm = InventarioViewModel(inventarioRepository, productoRepository)
        testScheduler.advanceUntilIdle()

        vm.loadInventories(forceRefresh = true)
        testScheduler.advanceTimeBy(1)
        assertTrue(vm.isLoading.value)

        testScheduler.advanceUntilIdle()
        assertFalse(vm.isLoading.value)

        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `loadInventories - debe cargar inventarios exitosamente`() = runTest {
        val mockInventarios = listOf(
            createMockInventory(1, "Harina", "DISPONIBLE"),
            createMockInventory(2, "Azúcar", "BAJO STOCK")
        )
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns Result.success(mockInventarios)

        viewModel.loadInventories(forceRefresh = true)

        assertNull(viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
        coVerify(exactly = 1) { inventarioRepository.fetchAllActiveInventories(true) }
    }

    // ============================================================
    // TESTS: Filtrado de inventarios
    // ============================================================

    @Test
    fun `inventoryFiltered - debe filtrar por query de búsqueda`() = runTest {
        val inventarios = listOf(
            createMockInventory(1, "Harina de Trigo", "DISPONIBLE", "Granos"),
            createMockInventory(2, "Azúcar Blanca", "DISPONIBLE", "Azúcares"),
            createMockInventory(3, "Arroz", "BAJO STOCK", "Granos")
        )
        mockInventariosFlow.value = inventarios

        viewModel.updateSearchQuery("harina")

        val filtrados = viewModel.inventoryFiltered.first()
        assertEquals(1, filtrados.size)
        assertEquals("Harina de Trigo", filtrados.first().nombreProducto)
    }

    @Test
    fun `inventoryFiltered - debe filtrar por categoría`() = runTest {
        val inventarios = listOf(
            createMockInventory(1, "Harina", "DISPONIBLE", "Granos"),
            createMockInventory(2, "Azúcar", "DISPONIBLE", "Azúcares"),
            createMockInventory(3, "Arroz", "BAJO STOCK", "Granos")
        )
        mockInventariosFlow.value = inventarios

        viewModel.updateCategoriaFilter("Granos")

        val filtrados = viewModel.inventoryFiltered.first()
        assertEquals(2, filtrados.size)
        assertTrue(filtrados.all { it.nombreCategoria == "Granos" })
    }

    @Test
    fun `inventoryFiltered - debe filtrar por estado`() = runTest {
        val inventarios = listOf(
            createMockInventory(1, "Harina", "DISPONIBLE"),
            createMockInventory(2, "Azúcar", "BAJO STOCK"),
            createMockInventory(3, "Arroz", "DISPONIBLE")
        )
        mockInventariosFlow.value = inventarios

        viewModel.updateSelectedEstado("DISPONIBLE")

        val filtrados = viewModel.inventoryFiltered.first()
        assertEquals(2, filtrados.size)
        assertTrue(filtrados.all { it.estadoStock == "DISPONIBLE" })
    }

    @Test
    fun `inventoryFiltered - debe combinar múltiples filtros`() = runTest {
        val inventarios = listOf(
            createMockInventory(1, "Harina de Trigo", "DISPONIBLE", "Granos"),
            createMockInventory(2, "Harina de Maíz", "BAJO STOCK", "Granos"),
            createMockInventory(3, "Azúcar", "DISPONIBLE", "Azúcares"),
            createMockInventory(4, "Arroz", "DISPONIBLE", "Granos")
        )
        mockInventariosFlow.value = inventarios

        viewModel.updateSearchQuery("harina")
        viewModel.updateCategoriaFilter("Granos")
        viewModel.updateSelectedEstado("DISPONIBLE")

        val filtrados = viewModel.inventoryFiltered.first()
        assertEquals(1, filtrados.size)
        assertEquals("Harina de Trigo", filtrados.first().nombreProducto)
    }

    @Test
    fun `clearFilters - debe limpiar todos los filtros`() = runTest {
        viewModel.updateSearchQuery("test")
        viewModel.updateCategoriaFilter("Granos")
        viewModel.updateSelectedEstado("DISPONIBLE")

        viewModel.clearFilters()

        assertEquals("", viewModel.searchQuery.value)
        assertEquals("Todos", viewModel.selectedCategoria.value)
        assertEquals("Todos", viewModel.selectedEstado.value)
    }

    // ============================================================
    // TESTS: Paginación
    // ============================================================

    @Test
    fun `totalPages - debe calcular correctamente el total de páginas`() = runTest {
        val inventarios = (1..25).map { createMockInventory(it, "Producto$it", "DISPONIBLE") }
        mockInventariosFlow.value = inventarios

        val total = viewModel.totalPages.first()
        assertEquals(3, total)
    }



    // ============================================================
    // TESTS: createInventoryWithProduct()
    // ============================================================

    @Test
    fun `createInventoryWithProduct - debe crear inventario exitosamente`() = runTest {
        val dto = createMockInventoryCreateDTO(1, "Harina")
        val mockInventarios = listOf(createMockInventory(1, "Harina", "DISPONIBLE"))

        coEvery { inventarioRepository.createInventoryWithProduct(any()) } just Runs
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns Result.success(mockInventarios)

        viewModel.createInventoryWithProduct(dto)

        assertTrue(viewModel.successMessage.value?.contains("Harina") == true)
        assertFalse(viewModel.isSaving.value)
        coVerify(exactly = 1) { inventarioRepository.createInventoryWithProduct(dto) }
        coVerify(exactly = 1) { inventarioRepository.fetchAllActiveInventories(true) }
    }

    @Test
    fun `createInventoryWithProduct - debe activar isSaving durante la creación`() = runTest {
        val testScheduler = TestCoroutineScheduler()
        val testDispatcher2 = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher2)

        val dto = createMockInventoryCreateDTO(1, "Harina")

        coEvery { inventarioRepository.createInventoryWithProduct(any()) } coAnswers {
            delay(100)
        }
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns Result.success(emptyList())

        val vm = InventarioViewModel(inventarioRepository, productoRepository)
        testScheduler.advanceUntilIdle()

        vm.createInventoryWithProduct(dto)
        testScheduler.advanceTimeBy(1)
        assertTrue(vm.isSaving.value)

        testScheduler.advanceUntilIdle()
        assertFalse(vm.isSaving.value)

        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `createInventoryWithProduct - debe manejar error en creación`() = runTest {
        val dto = createMockInventoryCreateDTO(1, "Harina")
        coEvery { inventarioRepository.createInventoryWithProduct(any()) } throws Exception("Error al crear")

        viewModel.createInventoryWithProduct(dto)

        assertTrue(viewModel.errorMessage.value?.contains("Error") == true)
        assertFalse(viewModel.isSaving.value)
    }

    // ============================================================
    // TESTS: updateInventoryWithProduct()
    // ============================================================

    @Test
    fun `updateInventoryWithProduct - debe validar nombre requerido`() = runTest {
        val dto = createMockInventory(1, "", "DISPONIBLE")

        viewModel.updateInventoryWithProduct(dto)

        assertTrue(viewModel.errorMessage.value?.contains("nombre") == true)
        assertFalse(viewModel.isSaving.value)
    }

    @Test
    fun `updateInventoryWithProduct - debe validar categoría requerida`() = runTest {
        val dto = createMockInventory(1, "Harina", "DISPONIBLE", categoria = null)

        viewModel.updateInventoryWithProduct(dto)

        assertTrue(viewModel.errorMessage.value?.contains("categoría") == true)
        assertFalse(viewModel.isSaving.value)
    }

    @Test
    fun `updateInventoryWithProduct - debe validar unidad de medida requerida`() = runTest {
        val dto = createMockInventory(1, "Harina", "DISPONIBLE", unidadMedida = null)

        viewModel.updateInventoryWithProduct(dto)

        assertTrue(viewModel.errorMessage.value?.contains("unidad") == true)
        assertFalse(viewModel.isSaving.value)
    }

    @Test
    fun `updateInventoryWithProduct - debe calcular estado DISPONIBLE correctamente`() = runTest {
        val dto = InventoryWithProductResponseAnswerUpdateDTO(
            idInventario = 1,
            idProducto = 10,
            nombreProducto = "Harina",
            descripcionProducto = "Desc",
            nombreCategoria = "Granos",
            unidadMedida = "kg",
            stock = 100.0,
            stockLimitMin = 50.0,
            estadoStock = "" // Se calculará
        )

        coEvery { inventarioRepository.updateInventoryWithProduct(any()) } just Runs
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns Result.success(emptyList())

        viewModel.updateInventoryWithProduct(dto)

        val slot = slot<InventoryWithProductResponseAnswerUpdateDTO>()
        coVerify { inventarioRepository.updateInventoryWithProduct(capture(slot)) }
        assertEquals("DISPONIBLE", slot.captured.estadoStock)
    }

    @Test
    fun `updateInventoryWithProduct - debe calcular estado BAJO STOCK correctamente`() = runTest {
        val dto = InventoryWithProductResponseAnswerUpdateDTO(
            idInventario = 1,
            idProducto = 10,
            nombreProducto = "Harina",
            descripcionProducto = "Desc",
            nombreCategoria = "Granos",
            unidadMedida = "kg",
            stock = 30.0,
            stockLimitMin = 50.0,
            estadoStock = ""
        )

        coEvery { inventarioRepository.updateInventoryWithProduct(any()) } just Runs
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns Result.success(emptyList())

        viewModel.updateInventoryWithProduct(dto)

        val slot = slot<InventoryWithProductResponseAnswerUpdateDTO>()
        coVerify { inventarioRepository.updateInventoryWithProduct(capture(slot)) }
        assertEquals("BAJO STOCK", slot.captured.estadoStock)
    }

    @Test
    fun `updateInventoryWithProduct - debe calcular estado AGOTADO correctamente`() = runTest {
        val dto = InventoryWithProductResponseAnswerUpdateDTO(
            idInventario = 1,
            idProducto = 10,
            nombreProducto = "Harina",
            descripcionProducto = "Desc",
            nombreCategoria = "Granos",
            unidadMedida = "kg",
            stock = 0.0,
            stockLimitMin = 50.0,
            estadoStock = ""
        )

        coEvery { inventarioRepository.updateInventoryWithProduct(any()) } just Runs
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns Result.success(emptyList())

        viewModel.updateInventoryWithProduct(dto)

        val slot = slot<InventoryWithProductResponseAnswerUpdateDTO>()
        coVerify { inventarioRepository.updateInventoryWithProduct(capture(slot)) }
        assertEquals("AGOTADO", slot.captured.estadoStock)
    }

    @Test
    fun `updateInventoryWithProduct - debe calcular estado NO ASIGNADO cuando stockMin es 0`() = runTest {
        val dto = InventoryWithProductResponseAnswerUpdateDTO(
            idInventario = 1,
            idProducto = 10,
            nombreProducto = "Harina",
            descripcionProducto = "Desc",
            nombreCategoria = "Granos",
            unidadMedida = "kg",
            stock = 100.0,
            stockLimitMin = 0.0,
            estadoStock = ""
        )

        coEvery { inventarioRepository.updateInventoryWithProduct(any()) } just Runs
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns Result.success(emptyList())

        viewModel.updateInventoryWithProduct(dto)

        val slot = slot<InventoryWithProductResponseAnswerUpdateDTO>()
        coVerify { inventarioRepository.updateInventoryWithProduct(capture(slot)) }
        assertEquals("NO ASIGNADO", slot.captured.estadoStock)
    }

    @Test
    fun `updateInventoryWithProduct - debe actualizar exitosamente`() = runTest {
        val dto = createMockInventory(1, "Harina", "DISPONIBLE")
        val mockInventarios = listOf(dto)

        coEvery { inventarioRepository.updateInventoryWithProduct(any()) } just Runs
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns Result.success(mockInventarios)

        viewModel.updateInventoryWithProduct(dto)

        assertTrue(viewModel.successMessage.value?.contains("Harina") == true)
        assertFalse(viewModel.isSaving.value)
        coVerify(exactly = 1) { inventarioRepository.updateInventoryWithProduct(any()) }
    }

    @Test
    fun `updateInventoryWithProduct - debe manejar error en actualización`() = runTest {
        val dto = createMockInventory(1, "Harina", "DISPONIBLE")
        coEvery { inventarioRepository.updateInventoryWithProduct(any()) } throws Exception("Error al actualizar")

        viewModel.updateInventoryWithProduct(dto)

        assertTrue(viewModel.errorMessage.value?.contains("Error") == true)
        assertFalse(viewModel.isSaving.value)
    }

    // ============================================================
    // TESTS: deleteInventory()
    // ============================================================

    @Test
    fun `deleteInventory - debe eliminar inventario exitosamente`() = runTest {
        val idInventario = 1
        coEvery { inventarioRepository.logicalDeleteInventory(idInventario) } returns Result.success(Unit)
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns Result.success(emptyList())

        viewModel.deleteInventory(idInventario, "Harina")

        assertTrue(viewModel.successMessage.value?.contains("eliminado") == true)
        assertFalse(viewModel.isLoading.value)
        coVerify(exactly = 1) { inventarioRepository.logicalDeleteInventory(idInventario) }
    }

    @Test
    fun `deleteInventory - debe manejar error al eliminar`() = runTest {
        val idInventario = 1
        coEvery { inventarioRepository.logicalDeleteInventory(idInventario) } returns
                Result.failure(Exception("Error al eliminar"))

        viewModel.deleteInventory(idInventario, "Harina")

        assertTrue(viewModel.errorMessage.value?.contains("Error al eliminar") == true)
        assertFalse(viewModel.isLoading.value)
    }

    // ============================================================
    // TESTS: Categorías y Unidades
    // ============================================================

    @Test
    fun `loadCategorias - debe cargar categorías del repositorio`() = runTest {
        val mockCategorias = listOf("Granos", "Lácteos", "Carnes")
        coEvery { productoRepository.fetchCategoriasActivas(true) } returns Result.success(mockCategorias)

        viewModel.loadCategorias(forceRefresh = true)

        coVerify(exactly = 1) { productoRepository.fetchCategoriasActivas(true) }
    }

    @Test
    fun `loadUnidadesMedida - debe cargar unidades del repositorio`() = runTest {
        val mockUnidades = listOf("kg", "gr", "litros")
        coEvery { productoRepository.fetchUnidadesMedidaActivas(true) } returns Result.success(mockUnidades)

        viewModel.loadUnidadesMedida(forceRefresh = true)

        coVerify(exactly = 1) { productoRepository.fetchUnidadesMedidaActivas(true) }
    }

    // ============================================================
    // TESTS: Estados disponibles
    // ============================================================

    @Test
    fun `estados - debe extraer estados únicos y ordenados`() = runTest {
        val inventarios = listOf(
            createMockInventory(1, "Harina", "DISPONIBLE"),
            createMockInventory(2, "Azúcar", "BAJO STOCK"),
            createMockInventory(3, "Arroz", "AGOTADO"),
            createMockInventory(4, "Sal", "DISPONIBLE")
        )
        mockInventariosFlow.value = inventarios

        val estados = viewModel.estados.first()
        assertEquals(3, estados.size)
        assertTrue(estados.contains("DISPONIBLE"))
        assertTrue(estados.contains("BAJO STOCK"))
        assertTrue(estados.contains("AGOTADO"))
    }

    // ============================================================
    // TESTS: Utilidades
    // ============================================================

    @Test
    fun `clearError - debe limpiar mensaje de error`() = runTest {
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns
                Result.failure(Exception("Error"))
        viewModel.loadInventories(forceRefresh = true)

        viewModel.clearError()

        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `clearSuccess - debe limpiar mensaje de éxito`() = runTest {
        val dto = createMockInventoryCreateDTO(1, "Harina")
        coEvery { inventarioRepository.createInventoryWithProduct(any()) } just Runs
        coEvery { inventarioRepository.fetchAllActiveInventories(true) } returns Result.success(emptyList())

        viewModel.createInventoryWithProduct(dto)

        viewModel.clearSuccess()

        assertNull(viewModel.successMessage.value)
    }

    @Test
    fun `getInventory - debe obtener inventario del cache del repositorio`() = runTest {
        val mockInventory = createMockInventory(1, "Harina", "DISPONIBLE")
        every { inventarioRepository.getInventoryFromCache(1) } returns mockInventory

        val inventario = viewModel.getInventory(1)

        assertNotNull(inventario)
        assertEquals("Harina", inventario?.nombreProducto)
        verify(exactly = 1) { inventarioRepository.getInventoryFromCache(1) }
    }

    // ============================================================
    // Helpers
    // ============================================================

    private fun createMockInventory(
        id: Int,
        nombre: String,
        estado: String,
        categoria: String? = "Granos",
        unidadMedida: String? = "kg"
    ): InventoryWithProductResponseAnswerUpdateDTO {
        return InventoryWithProductResponseAnswerUpdateDTO(
            idInventario = id,
            idProducto = id * 10,
            nombreProducto = nombre,
            descripcionProducto = "Descripción de $nombre",
            nombreCategoria = categoria,
            unidadMedida = unidadMedida,
            stock = 100.0,
            stockLimitMin = 50.0,
            estadoStock = estado
        )
    }

    private fun createMockInventoryCreateDTO(
        idProducto: Int,
        nombre: String
    ): InventoryWithProductCreateDTO {
        return InventoryWithProductCreateDTO(
            idInventario = 0,
            idProducto = idProducto,
            nombreProducto = nombre,
            descripcionProducto = "Descripción",
            nombreCategoria = "Granos",
            unidadMedida = "kg",
            stock = 100.0,
            stockLimitMin = 50.0
        )
    }
}