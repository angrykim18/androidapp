package com.example.newez.model

import com.google.gson.annotations.SerializedName

data class VodContent(
    @SerializedName("id")
    val id: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("posterPath")
    val posterPath: String?,

    @SerializedName("categoryId")
    val categoryId: Long,

    // ✅ [수정] 기본값을 null로 지정하여, 이 값이 없어도 에러가 발생하지 않도록 합니다.
    @SerializedName("lastWatchedEpisodeId")
    val lastWatchedEpisodeId: Long? = null,

    @SerializedName("lastWatchedTimestamp")
    val lastWatchedTimestamp: Int? = null,

    @SerializedName("lastWatchedEpisodeNumber")
    val lastWatchedEpisodeNumber: String? = null
)