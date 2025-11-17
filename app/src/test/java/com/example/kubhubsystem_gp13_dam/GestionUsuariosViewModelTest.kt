package com.example.kubhubsystem_gp13_dam.viewmodel

import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.model.Usuario
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import com.example.kubhubsystem_gp13_dam.repository.RolRepository
import com.example.kubhubsystem_gp13_dam.local.dto.UsuarioEstadisticasDTO
import com.example.kubhubsystem_gp13_dam.local.dto.RolResponseDTO
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GestionUsuariosViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var viewModel: GestionUsuariosViewModel
    private lateinit var usuarioRepo: UsuarioRepository
    private lateinit var rolRepo: RolRepository
/*
    @Rule @JvmField
    val watcher: TestRule = object : TestWatcher() {

        override fun starting(description: Description) {
            println("➡️ Iniciando test: ${description.methodName}")
        }

        override fun succeeded(description: Description) {
            println("✅ Test pasado: ${description.methodName}")
        }

        override fun failed(e: Throwable, description: Description) {
            println("❌ Test falló: ${description.methodName}")
            println("   Motivo: ${e.message}")
        }

        override fun finished(description: Description) {
            println("🔚 Finalizado test: ${description.methodName}")
            println("--------------------------------------------")
        }
    }
*/

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        usuarioRepo = mockk(relaxed = true)
        rolRepo = mockk(relaxed = true)

        // Respuestas por defecto para inicialización
        coEvery { usuarioRepo.obtenerTodos() } returns emptyList()
        coEvery { rolRepo.obtenerTodos() } returns emptyList()
        coEvery { usuarioRepo.obtenerEstadisticas() } returns mockk()

        viewModel = GestionUsuariosViewModel(
            usuarioRepository = usuarioRepo,
            rolRepository = rolRepo
        )

        dispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private fun u(
        id: Int,
        nombre: String,
        rol: Rol = Rol.ADMINISTRADOR,
        activo: Boolean = true,
        correo: String = "$nombre@test.com"
    ) = Usuario(
        idUsuario = id,
        rol = rol,
        primerNombre = nombre,
        segundoNombre = null,
        apellidoPaterno = null,
        apellidoMaterno = null,
        email = correo,
        username = nombre.lowercase(),
        password = "123",
        activo = activo
    )

    private fun inyectarUsuarios(vararg usuarios: Usuario) {
        val campo = viewModel.javaClass.getDeclaredField("_estado")
        campo.isAccessible = true
        val flow = campo.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<GestionUsuariosEstado>
        flow.value = flow.value.copy(usuarios = usuarios.toList())
    }

    // ==========================================================
    // 🟩 TESTS DE FILTRADO
    // ==========================================================

    @Test
    fun `filtrar Todos muestra todos los usuarios`() = runTest {
        inyectarUsuarios(u(1, "Pedro"), u(2, "Juan"))

        viewModel.onFiltroRolChange("Todos")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.estado.value.usuariosFiltrados.size)
    }

    @Test
    fun `filtrar por texto busca en nombre-email-username`() = runTest {
        val p = u(1, "Pedro")
        val j = u(2, "Juan")

        inyectarUsuarios(p, j)

        viewModel.onBuscarTextoChange("ped")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("Pedro"), viewModel.estado.value.usuariosFiltrados.map { it.primerNombre })
    }

    @Test
    fun `filtrar por rol devuelve solo los de ese rol`() = runTest {
        val admin = u(1, "Pedro", Rol.ADMINISTRADOR)
        val doc = u(2, "Juan", Rol.DOCENTE)

        inyectarUsuarios(admin, doc)

        viewModel.onFiltroRolChange("Docente")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("Juan"), viewModel.estado.value.usuariosFiltrados.map { it.primerNombre })
    }

    @Test
    fun `filtrar Activos solo devuelve activos`() = runTest {
        inyectarUsuarios(u(1, "Pedro", activo = true), u(2, "Juan", activo = false))

        viewModel.onFiltroEstadoChange("Activos")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("Pedro"), viewModel.estado.value.usuariosFiltrados.map { it.primerNombre })
    }

    @Test
    fun `filtrar Inactivos solo devuelve inactivos`() = runTest {
        inyectarUsuarios(u(1, "Pedro", activo = true), u(2, "Juan", activo = false))

        viewModel.onFiltroEstadoChange("Inactivos")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("Juan"), viewModel.estado.value.usuariosFiltrados.map { it.primerNombre })
    }

    @Test
    fun `filtros combinados texto-rol-estado`() = runTest {
        inyectarUsuarios(
            u(1, "Pedro", rol = Rol.DOCENTE, activo = true),
            u(2, "Pedro", rol = Rol.ADMINISTRADOR, activo = true),
            u(3, "Pedro", rol = Rol.DOCENTE, activo = false),
        )

        viewModel.onBuscarTextoChange("ped")
        viewModel.onFiltroRolChange("Docente")
        viewModel.onFiltroEstadoChange("Activos")

        dispatcher.scheduler.advanceUntilIdle()

        val lista = viewModel.estado.value.usuariosFiltrados

        assertEquals(1, lista.size)
        assertEquals(Rol.DOCENTE, lista.first().rol)
        assertTrue(lista.first().activo)
    }

    @Test
    fun `ordenamiento alfabetico`() = runTest {
        inyectarUsuarios(
            u(1, "Pedro"),
            u(2, "Andres"),
            u(3, "Maria")
        )

        viewModel.onFiltroRolChange("Todos")
        dispatcher.scheduler.advanceUntilIdle()

        val nombres = viewModel.estado.value.usuariosFiltrados.map { it.primerNombre }
        assertEquals(listOf("Andres", "Maria", "Pedro"), nombres)
    }

    // ==========================================================
    // 🟦 TESTS CARGA DE DATOS
    // ==========================================================

    @Test
    fun `cargarDatosCompletos actualiza lista de usuarios`() = runTest {
        val lista = listOf(u(1, "Pedro"), u(2, "Juan"))

        coEvery { usuarioRepo.obtenerTodos() } returns lista
        coEvery { rolRepo.obtenerTodos() } returns listOf(mockk<RolResponseDTO>())
        coEvery { usuarioRepo.obtenerEstadisticas() } returns mockk<UsuarioEstadisticasDTO>()

        viewModel.cargarDatosCompletos()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.estado.value.usuarios.size)
    }

    @Test
    fun `cargarUsuarios actualiza solo usuarios`() = runTest {
        val lista = listOf(u(1, "Pedro"), u(2, "Juan"))
        coEvery { usuarioRepo.obtenerTodos() } returns lista

        viewModel.cargarUsuarios()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.estado.value.usuarios.size)
    }

    // ==========================================================
    // 🟧 CREAR USUARIO
    // ==========================================================

    @Test
    fun `crearUsuario exitoso llama a cargarDatos`() = runTest {
        val nuevo = u(99, "Nuevo")
        coEvery { usuarioRepo.crear(any()) } returns nuevo
        coEvery { usuarioRepo.obtenerTodos() } returns listOf(nuevo)

        viewModel.crearUsuario("Nuevo", null, null, null, "n@test.com", "nuevo", "123", Rol.DOCENTE)

        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Usuario creado correctamente", viewModel.estado.value.mensajeExito)
    }

    @Test
    fun `crearUsuario fallo retorna error`() = runTest {
        coEvery { usuarioRepo.crear(any()) } returns null

        viewModel.crearUsuario("Nuevo", null, null, null, "n@test.com", "nuevo", "123", Rol.DOCENTE)

        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("No se pudo crear el usuario", viewModel.estado.value.error)
    }

    // ==========================================================
    // 🟨 ACTUALIZAR USUARIO
    // ==========================================================

    @Test
    fun `actualizarUsuario exitoso llama cargarDatos`() = runTest {
        val usr = u(1, "Pedro")

        coEvery { usuarioRepo.actualizar(1, any()) } returns usr
        coEvery { usuarioRepo.obtenerTodos() } returns listOf(usr)

        viewModel.actualizarUsuario(usr)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Usuario actualizado correctamente", viewModel.estado.value.mensajeExito)
    }

    @Test
    fun `actualizarUsuario fallido reporta error`() = runTest {
        val usr = u(1, "Pedro")

        coEvery { usuarioRepo.actualizar(any(), any()) } returns null

        viewModel.actualizarUsuario(usr)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("No se pudo actualizar el usuario", viewModel.estado.value.error)
    }

    // ==========================================================
    // 🟥 ELIMINAR USUARIO
    // ==========================================================

    @Test
    fun `eliminar usuario exitoso recarga datos`() = runTest {
        val usr = u(1, "Pedro")

        coEvery { usuarioRepo.eliminar(1) } returns true
        coEvery { usuarioRepo.obtenerTodos() } returns emptyList()

        viewModel.eliminarUsuario(usr)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Usuario eliminado correctamente", viewModel.estado.value.mensajeExito)
    }

    @Test
    fun `eliminar usuario fallido`() = runTest {
        val usr = u(1, "Pedro")

        coEvery { usuarioRepo.eliminar(1) } returns false

        viewModel.eliminarUsuario(usr)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("No se pudo eliminar el usuario", viewModel.estado.value.error)
    }

    // ==========================================================
    // 🟪 ACTIVAR / DESACTIVAR USUARIO
    // ==========================================================

    @Test
    fun `activar usuario exitoso`() = runTest {
        coEvery { usuarioRepo.activar(1) } returns true
        coEvery { usuarioRepo.obtenerTodos() } returns emptyList()

        viewModel.activarUsuario(1)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Usuario activado correctamente", viewModel.estado.value.mensajeExito)
    }

    @Test
    fun `desactivar usuario exitoso`() = runTest {
        coEvery { usuarioRepo.desactivar(1) } returns true
        coEvery { usuarioRepo.obtenerTodos() } returns emptyList()

        viewModel.desactivarUsuario(1)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Usuario desactivado correctamente", viewModel.estado.value.mensajeExito)
    }

    // ==========================================================
    // 🟫 LIMPIAR MENSAJES
    // ==========================================================

    @Test
    fun `limpiar mensajes deja error y exito en null`() = runTest {
        val campo = viewModel.javaClass.getDeclaredField("_estado")
        campo.isAccessible = true
        val flow = campo.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<GestionUsuariosEstado>

        flow.value = flow.value.copy(error = "x", mensajeExito = "y")

        viewModel.limpiarMensajes()

        assertNull(viewModel.estado.value.error)
        assertNull(viewModel.estado.value.mensajeExito)
    }

    // ==========================================================
    // 🟦 FUNCIONES UTILITARIAS
    // ==========================================================

    @Test
    fun `obtenerUsuarioPorId devuelve match`() = runTest {
        inyectarUsuarios(u(1, "Pedro"), u(2, "Juan"))

        assertEquals("Juan", viewModel.obtenerUsuarioPorId(2)?.primerNombre)
    }


    @Test
    fun `esUsuarioDocente devuelve true cuando rol es DOCENTE`() = runTest {
        inyectarUsuarios(u(1, "Pedro", rol = Rol.DOCENTE))

        assertTrue(viewModel.esUsuarioDocente(1))
    }
}
