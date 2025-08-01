package com.example.newez.model

// ✅ [추가] VOD 컨텐츠 하나의 데이터를 담을 클래스
data class VodContent(
    val id: Long,
    val title: String,
    val description: String?,
    val posterPath: String?,
    val categoryId: Long
)