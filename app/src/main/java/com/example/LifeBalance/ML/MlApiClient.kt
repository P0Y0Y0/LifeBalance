package com.example.LifeBalance.ML

import com.example.LifeBalance.network.MLApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MlApiClient {
    val api: MLApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MLApiService::class.java)
    }
}