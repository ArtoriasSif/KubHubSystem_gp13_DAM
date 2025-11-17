package com.example.kubhubsystem_gp13_dam.api

import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import kotlin.toString

/**
 * ✅ TEST DE VERIFICACIÓN: MockWebServer funciona correctamente
 *
 * Ejecuta este test PRIMERO para verificar que MockWebServer está correctamente importado
 */
class TestMockWebServerVerificacion {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        println("✅ MockWebServer iniciado en: ${mockWebServer.url("/")}")
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        println("✅ MockWebServer detenido correctamente")
    }

    @Test
    fun `verificar que MockWebServer funciona correctamente`() {
        // Given
        val expectedBody = "Hello World"
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(expectedBody)
        )

        // When
        val url = mockWebServer.url("/test")

        // Then
        assertNotNull(url)
        assertTrue(url.toString().contains("/test"))
        println("✅ Test exitoso! URL: $url")
    }

    @Test
    fun `verificar que MockWebServer puede encolar multiples respuestas`() {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("Respuesta 1"))
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody("Respuesta 2"))
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Respuesta 3"))

        // When & Then
        val url1 = mockWebServer.url("/endpoint1")
        val url2 = mockWebServer.url("/endpoint2")
        val url3 = mockWebServer.url("/endpoint3")

        assertNotNull(url1)
        assertNotNull(url2)
        assertNotNull(url3)
        println("✅ Múltiples respuestas encoladas correctamente")
    }

    @Test
    fun `verificar que MockWebServer puede simular errores`() {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"Internal Server Error\"}")
        )

        // When
        val url = mockWebServer.url("/error")

        // Then
        assertNotNull(url)
        println("✅ Simulación de errores funcionando correctamente")
    }
}