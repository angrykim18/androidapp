package com.example.newez.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // ✅ [추가] 백엔드 서버의 기본 주소입니다.
    // 10.0.2.2는 안드로이드 에뮬레이터가 개발용 컴퓨터의 localhost를 가리키는 특별한 주소입니다.
    private const val BASE_URL = "http://192.168.0.2:8081/"

    // ✅ [추가] Retrofit 통신 객체를 생성하는 부분
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ✅ [추가] ApiService 인터페이스를 실제로 구현하여 사용할 수 있도록 만드는 부분
    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}