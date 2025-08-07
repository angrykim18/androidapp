package com.example.newez.model

import com.google.gson.annotations.SerializedName

data class VodFile(
    @SerializedName("id")
    val id: Long,

    @SerializedName("vodFileNumber")
    val vodFileNumber: String, // 앱 노출용 이름 (예: "1회")

    @SerializedName("vodFileName")
    val vodFileName: String,   // 실제 영상 파일 이름 (예: "episode_01.mp4")

    @SerializedName("vodContentId")
    val vodContentId: Long,

    // ✅ [추가] 백엔드에서 조합해주는 최종 영상 전체 URL
    @SerializedName("fullUrl")
    val fullUrl: String
)