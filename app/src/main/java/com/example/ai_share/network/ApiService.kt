package com.example.ai_share.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Path

interface ApiService {
    @Multipart
    @POST("/train")
    fun trainPerson(
        @Part("person_name") personName: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Call<TrainResponse>

    @Multipart
    @POST("/predict")
    fun predictPersons(
        @Part images: List<MultipartBody.Part>
    ): Call<PredictGroupResponse>

    @GET("/models")
    fun getModels(): Call<List<String>>

    @DELETE("/models/{model_name}")
    fun deleteModel(@Path("model_name") modelName: String): Call<Void>
}

data class TrainResponse(
    val success: Boolean,
    val model_path: String?,
    val error: String?
)

data class PredictResponse(
    val result: String,
    val accuracy: Float?,
    val image: String?
) 