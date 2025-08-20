package com.example.newez.model

// ✅ [추가] 페이지네이션 응답 전체를 담을 제네릭 클래스
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
    val number: Int // 현재 페이지 번호 (0부터 시작)
)