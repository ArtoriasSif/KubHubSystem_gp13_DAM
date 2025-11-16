package com.example.kubhubsystem_gp13_dam.local.remote

import com.example.kubhubsystem_gp13_dam.ui.model.RecipeWithDetailsAnswerUpdateDTO
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeWithDetailsCreateDTO
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RecetaApiService {

    /**
     * 🟢 Obtener todas las recetas activas con detalles
     * GET http://localhost:8080/api/v1/receta/find-all-recipe-with-details-active/
     */
    @GET("api/v1/receta/find-all-recipe-with-details-active/")
    suspend fun findAllRecipeWithDetailsActive(): List<RecipeWithDetailsAnswerUpdateDTO>


    /**
     * 🟢 Crear receta con detalles
     * POST http://localhost:8080/api/v1/receta/create-recipe-with-details/
     */
    @POST("api/v1/receta/create-recipe-with-details/")
    suspend fun createRecipeWithDetails(
        @Body dto: RecipeWithDetailsCreateDTO
    ): RecipeWithDetailsCreateDTO


    /**
     * 🟡 Actualizar receta con detalles
     * PUT http://localhost:8080/api/v1/receta/update-recipe-with-details/
     */
    @PUT("api/v1/receta/update-recipe-with-details/")
    suspend fun updateRecipeWithDetails(
        @Body dto: RecipeWithDetailsAnswerUpdateDTO
    ): RecipeWithDetailsAnswerUpdateDTO


    /**
     * 🟡 Cambiar estado (Activo / Inactivo) de una receta con detalles
     */
    @PUT("api/v1/receta/update-changing-status-recipe-with/{id_receta}")
    suspend fun updateChangingStatusRecipeWith(
        @Path("id_receta") idReceta: Int
    ): Response<ResponseBody>

    /**
     * 🔴 Desactivar receta (status = false)
     * PUT http://localhost:8080/api/v1/receta/update-status-active-false-recipe-with-details/{id_receta}
     */
    @PUT("api/v1/receta/update-status-active-false-recipe-with-details/{id_receta}")
    suspend fun updateStatusActiveFalseRecipe(
        @Path("id_receta") idReceta: Int
    ): retrofit2.Response<Unit>
}
