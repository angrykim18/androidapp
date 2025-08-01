package com.example.newez.model

// ✅ [추가] 회차(에피소드) 하나의 데이터를 담을 클래스
data class VodFile(
    val id: Long,
    val vodFileNumber: String, // 앱 노출용 이름 (예: "1회")
    val vodFileName: String,   // 실제 영상 전체 URL
    val vodContentId: Long,
)