package com.example.kubhubsystem_gp13_dam.api

import com.example.kubhubsystem_gp13_dam.local.remote.RecetaApiService
import com.example.kubhubsystem_gp13_dam.ui.model.EstadoRecetaType
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeItemDTO
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeWithDetailsAnswerUpdateDTO
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeWithDetailsCreateDTO
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.test.assertFailsWith

/**
 * ✅ PRUEBAS UNITARIAS PARA RecetaApiService
 *
 * Valida:
 * - URLs correctas
 * - Métodos HTTP correctos
 * - Mapeo JSON → DTO
 * - Manejo de códigos de respuesta
 * - Manejo de errores de red
 */
class RecetaApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: RecetaApiService
    private lateinit var gson: Gson

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        gson = Gson()

        val client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RecetaApiService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    // ============================================================
    // TESTS: findAllRecipeWithDetailsActive()
    // ============================================================

    @Test
    fun `findAllRecipeWithDetailsActive - debe retornar lista de recetas con 200 OK`() = runTest {
        // Given
        val mockRecetas = listOf(
            RecipeWithDetailsAnswerUpdateDTO(
                idReceta = 1,
                nombreReceta = "Pizza Margherita",
                descripcionReceta = "Pizza clásica",
                listaItems = listOf(
                    RecipeItemDTO(
                        idProducto = 10,
                        nombreProducto = "Harina",
                        cantUnidadMedida = 500.0,
                        unidadMedida = "gr",
                        activo = true
                    )
                ),
                instrucciones = "Mezclar y hornear",
                estadoReceta = EstadoRecetaType.ACTIVO,
                cambioReceta = false,
                cambioDetalles = false
            ),
            RecipeWithDetailsAnswerUpdateDTO(
                idReceta = 2,
                nombreReceta = "Ensalada César",
                descripcionReceta = "Ensalada fresca",
                listaItems = emptyList(),
                instrucciones = "Mezclar ingredientes",
                estadoReceta = EstadoRecetaType.ACTIVO,
                cambioReceta = false,
                cambioDetalles = false
            )
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(gson.toJson(mockRecetas))
        )

        // When
        val resultado = apiService.findAllRecipeWithDetailsActive()

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("/api/v1/receta/find-all-recipe-with-details-active") == true)

        assertEquals(2, resultado.size)
        assertEquals("Pizza Margherita", resultado[0].nombreReceta)
        assertEquals("Ensalada César", resultado[1].nombreReceta)
        assertEquals(EstadoRecetaType.ACTIVO, resultado[0].estadoReceta)
    }

    @Test
    fun `findAllRecipeWithDetailsActive - debe retornar lista vacía cuando no hay recetas`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
        )

        // When
        val resultado = apiService.findAllRecipeWithDetailsActive()

        // Then
        assertTrue(resultado.isEmpty())
    }

    @Test
    fun `findAllRecipeWithDetailsActive - debe lanzar excepción con 500`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )

        // When & Then
        try {
            apiService.findAllRecipeWithDetailsActive()
            fail("Debería haber lanzado HttpException")
        } catch (e: HttpException) {
            assertEquals(500, e.code())
        }
    }

    @Test
    fun `findAllRecipeWithDetailsActive - debe lanzar excepción con 404`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("Not Found")
        )

        // When & Then
        try {
            apiService.findAllRecipeWithDetailsActive()
            fail("Debería haber lanzado HttpException")
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }
    }

    // ============================================================
    // TESTS: createRecipeWithDetails()
    // ============================================================

    @Test
    fun `createRecipeWithDetails - debe enviar POST con DTO correcto y retornar receta creada`() = runTest {
        // Given
        val dtoCreate = RecipeWithDetailsCreateDTO(
            nombreReceta = "Pasta Carbonara",
            descripcionReceta = "Pasta italiana",
            listaItems = listOf(
                RecipeItemDTO(
                    idProducto = 5,
                    nombreProducto = "Pasta",
                    cantUnidadMedida = 200.0,
                    unidadMedida = "gr",
                    activo = true
                )
            ),
            instrucciones = "Cocinar pasta",
            estadoReceta = EstadoRecetaType.ACTIVO
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(gson.toJson(dtoCreate))
        )

        // When
        val resultado = apiService.createRecipeWithDetails(dtoCreate)

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("/api/v1/receta/create-recipe-with-details") == true)
        assertNotNull(request.body)

        assertEquals("Pasta Carbonara", resultado.nombreReceta)
        assertEquals("Pasta italiana", resultado.descripcionReceta)
        assertEquals(1, resultado.listaItems?.size)
    }

    @Test
    fun `createRecipeWithDetails - debe lanzar excepción con 400 Bad Request por datos inválidos`() = runTest {
        // Given
        val dtoInvalido = RecipeWithDetailsCreateDTO(
            nombreReceta = "", // nombre vacío
            descripcionReceta = null,
            listaItems = emptyList(),
            instrucciones = null,
            estadoReceta = EstadoRecetaType.ACTIVO
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("{\"error\": \"Nombre de receta requerido\"}")
        )

        // When & Then
        try {
            apiService.createRecipeWithDetails(dtoInvalido)
            fail("Debería haber lanzado HttpException")
        } catch (e: HttpException) {
            assertEquals(400, e.code())
        }
    }

    // ============================================================
    // TESTS: updateRecipeWithDetails()
    // ============================================================

    @Test
    fun `updateRecipeWithDetails - debe enviar PUT con DTO correcto y retornar receta actualizada`() = runTest {
        // Given
        val dtoUpdate = RecipeWithDetailsAnswerUpdateDTO(
            idReceta = 1,
            nombreReceta = "Pizza Actualizada",
            descripcionReceta = "Nueva descripción",
            listaItems = listOf(
                RecipeItemDTO(
                    idProducto = 10,
                    nombreProducto = "Harina",
                    cantUnidadMedida = 600.0,
                    unidadMedida = "gr",
                    activo = true
                )
            ),
            instrucciones = "Instrucciones actualizadas",
            estadoReceta = EstadoRecetaType.ACTIVO,
            cambioReceta = false,
            cambioDetalles = false
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(gson.toJson(dtoUpdate))
        )

        // When
        val resultado = apiService.updateRecipeWithDetails(dtoUpdate)

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path?.contains("/api/v1/receta/update-recipe-with-details") == true)

        assertEquals(1, resultado.idReceta)
        assertEquals("Pizza Actualizada", resultado.nombreReceta)
        assertEquals(600.0, resultado.listaItems?.first()?.cantUnidadMedida ?: 0.0, 0.01)
    }

    @Test
    fun `updateRecipeWithDetails - debe lanzar excepción con 404 si receta no existe`() = runTest {
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

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\": \"Receta no encontrada\"}")
        )

        // When & Then
        try {
            apiService.updateRecipeWithDetails(dtoUpdate)
            fail("Debería haber lanzado HttpException")
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }
    }

    // ============================================================
    // TESTS: updateChangingStatusRecipeWith()
    // ============================================================

    @Test
    fun `updateChangingStatusRecipeWith - debe enviar PUT y retornar respuesta exitosa`() = runTest {
        // Given
        val idReceta = 1

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{\"message\": \"Estado cambiado exitosamente\"}")
        )

        // When
        val response = apiService.updateChangingStatusRecipeWith(idReceta)

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path?.contains("/api/v1/receta/update-changing-status-recipe-with/1") == true)
        assertTrue(response.isSuccessful)
        assertEquals(200, response.code())
    }

    @Test
    fun `updateChangingStatusRecipeWith - debe retornar error 404 si receta no existe`() = runTest {
        // Given
        val idReceta = 9999

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\": \"Receta no encontrada\"}")
        )

        // When
        val response = apiService.updateChangingStatusRecipeWith(idReceta)

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    // ============================================================
    // TESTS: updateStatusActiveFalseRecipe()
    // ============================================================

    @Test
    fun `updateStatusActiveFalseRecipe - debe desactivar receta exitosamente`() = runTest {
        // Given
        val idReceta = 1

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("")
        )

        // When
        val response = apiService.updateStatusActiveFalseRecipe(idReceta)

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path?.contains("/api/v1/receta/update-status-active-false-recipe-with-details/1") == true)
        assertTrue(response.isSuccessful)
        assertEquals(200, response.code())
    }

    @Test
    fun `updateStatusActiveFalseRecipe - debe retornar error 404 si receta no existe`() = runTest {
        // Given
        val idReceta = 9999

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
        )

        // When
        val response = apiService.updateStatusActiveFalseRecipe(idReceta)

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    // ============================================================
    // TESTS: Manejo de errores de red
    // ============================================================

    @Test
    fun `findAllRecipeWithDetailsActive - debe lanzar timeout`() = runTest {
        // Given → el servidor NO responde nunca
        mockWebServer.enqueue(
            MockResponse().apply {
                setSocketPolicy(
                    okhttp3.mockwebserver.SocketPolicy.NO_RESPONSE
                )
            }
        )

        // When & Then
        assertFailsWith<SocketTimeoutException> {
            apiService.findAllRecipeWithDetailsActive()
        }
    }


    @Test
    fun `createRecipeWithDetails - debe mapear correctamente JSON complejo con items`() = runTest {
        // Given
        val jsonComplejo = """
            {
                "nombreReceta": "Receta Compleja",
                "descripcionReceta": "Descripción detallada",
                "listaItems": [
                    {
                        "idProducto": 1,
                        "nombreProducto": "Ingrediente 1",
                        "cantUnidadMedida": 100.5,
                        "unidadMedida": "gr",
                        "activo": true
                    },
                    {
                        "idProducto": 2,
                        "nombreProducto": "Ingrediente 2",
                        "cantUnidadMedida": 50.25,
                        "unidadMedida": "ml",
                        "activo": true
                    }
                ],
                "instrucciones": "Paso 1, Paso 2, Paso 3",
                "estadoReceta": "ACTIVO"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(jsonComplejo)
        )

        val dtoCreate = RecipeWithDetailsCreateDTO(
            nombreReceta = "Receta Compleja",
            descripcionReceta = "Descripción detallada",
            listaItems = listOf(
                RecipeItemDTO(
                    idProducto = 1,
                    nombreProducto = "Ingrediente 1",
                    cantUnidadMedida = 100.5,
                    unidadMedida = "gr",
                    activo = true
                ),
                RecipeItemDTO(
                    idProducto = 2,
                    nombreProducto = "Ingrediente 2",
                    cantUnidadMedida = 50.25,
                    unidadMedida = "ml",
                    activo = true
                )
            ),
            instrucciones = "Paso 1, Paso 2, Paso 3",
            estadoReceta = EstadoRecetaType.ACTIVO
        )

        // When
        val resultado = apiService.createRecipeWithDetails(dtoCreate)

        // Then
        assertEquals("Receta Compleja", resultado.nombreReceta)
        assertEquals(2, resultado.listaItems?.size)
        assertEquals(100.5, resultado.listaItems?.get(0)?.cantUnidadMedida ?: 0.0, 0.01)
        assertEquals("ml", resultado.listaItems?.get(1)?.unidadMedida)
    }
}