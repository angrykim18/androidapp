package com.example.newez.network

import okhttp3.OkHttpClient // ✅ [추가]
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // ✅ [추가]

object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.2:8081/"

    // ✅ [추가] 5초 타임아웃을 설정한 OkHttpClient 생성
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // ✅ [추가] 생성한 OkHttpClient를 Retrofit에 적용
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}