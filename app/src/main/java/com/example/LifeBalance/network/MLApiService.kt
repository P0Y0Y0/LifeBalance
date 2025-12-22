package com.example.LifeBalance.network

import com.example.LifeBalance.data_Model.MLRequest
import com.example.LifeBalance.data_Model.MLResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MLApiService {

    @POST("predict")
    suspend fun predictHealth(
        @Body request: MLRequest
    ): MLResponse
}
