package com.example.kubhubsystem_gp13_dam

import com.example.kubhubsystem_gp13_dam.data.repository.LoginRepository
import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.viewmodel.LoginViewModel
import io.mockk.*
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.test.assertEquals


@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    // -----------------------------------
    // TestWatcher → logs en consola
    // -----------------------------------
    @Rule @JvmField
    val watcher: TestRule = object : TestWatcher() {
        override fun starting(description: Description) {
            println("➡️ Iniciando test: ${description.methodName}")
        }
        override fun succeeded(description: Description) {
            println("✅ Test pasado: ${description.methodName}\n")
        }
        override fun failed(e: Throwable, description: Description) {
            println("❌ Test falló: ${description.methodName}")
            println("   Motivo: ${e.message}")
        }
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var loginRepository: LoginRepository
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        loginRepository = mockk(relaxed = true)
        viewModel = LoginViewModel(loginRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============================================================
    // PRUEBAS BÁSICAS DE ACTUALIZACIÓN DE ESTADO
    // ============================================================

    @Test
    fun `updateEmail actualiza email y limpia error`() {
        viewModel.updateEmail("test@test.com")
        assertEquals("test@test.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `updatePassword actualiza password y limpia error`() {
        viewModel.updatePassword("1234")
        assertEquals("1234", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `updateRememberSession cambia estado correctamente`() {
        viewModel.updateRememberSession(true)
        assertTrue(viewModel.uiState.value.rememberSession)
    }

    @Test
    fun `updateForgotPasswordRequest cambia el estado correctamente`() {
        viewModel.updateForgotPasswordRequest(true)
        assertTrue(viewModel.uiState.value.forgotPasswordRequested)
    }

    // ============================================================
    // DEMO ROLES
    // ============================================================

    @Test
    fun `selectDemoRole llena email y password`() {
        every { loginRepository.getDemoCredentials(Rol.ADMINISTRADOR) } returns
                Pair("admin@test.com", "123")

        viewModel.selectDemoRole(Rol.ADMINISTRADOR)

        assertEquals("admin@test.com", viewModel.uiState.value.email)
        assertEquals("123", viewModel.uiState.value.password)
        assertEquals(Rol.ADMINISTRADOR, viewModel.uiState.value.selectedRole)
    }

    @Test
    fun `clearDemoSelection reinicia credenciales`() {
        // Simula estado previo
        every { loginRepository.getDemoCredentials(Rol.DOCENTE) } returns Pair("doc@test.com", "abc")
        viewModel.selectDemoRole(Rol.DOCENTE)

        viewModel.clearDemoSelection()

        assertEquals("", viewModel.uiState.value.email)
        assertEquals("", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.selectedRole)
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @Test
    fun `login con campos vacíos retorna error inmediatamente`() {
        viewModel.login { Assert.fail("No debería llamar onSuccess") }
        assertEquals(
            "Por favor complete todos los campos obligatorios",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun `login exitoso llama onSuccess y apaga loading`() = runTest {
        viewModel.updateEmail("user@test.com")
        viewModel.updatePassword("1234")

        coEvery { loginRepository.login("user@test.com", "1234") } returns null

        var successCalled = false

        viewModel.login { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `login usuario no existe muestra error email`() = runTest {
        viewModel.updateEmail("no@test.com")
        viewModel.updatePassword("1234")

        coEvery { loginRepository.login(any(), any()) } returns "email"

        viewModel.login { Assert.fail("No debería llamar onSuccess") }
        advanceUntilIdle()

        assertEquals("El usuario no existe", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `login password incorrecta muestra error password`() = runTest {
        viewModel.updateEmail("user@test.com")
        viewModel.updatePassword("xxxx")

        coEvery { loginRepository.login(any(), any()) } returns "password"

        viewModel.login {}
        advanceUntilIdle()

        assertEquals("Contraseña incorrecta", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `login error de conexión`() = runTest {
        viewModel.updateEmail("user@test.com")
        viewModel.updatePassword("1234")

        coEvery { loginRepository.login(any(), any()) } returns "error"

        viewModel.login {}
        advanceUntilIdle()

        assertEquals("Error de conexión. Verifique su red.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `login excepción inesperada`() = runTest {
        viewModel.updateEmail("x@test.com")
        viewModel.updatePassword("123")

        coEvery { loginRepository.login(any(), any()) } throws RuntimeException("Boom")

        viewModel.login {}
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage!!.contains("Boom"))
    }
}
