package com.example.newez.model

// 서버의 Pageable 객체와 매칭되는 데이터 클래스
data class Page<T>(
    val content: List<T>,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)