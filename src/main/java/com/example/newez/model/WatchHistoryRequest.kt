package com.example.newez.model

import com.google.gson.annotations.SerializedName;

data class WatchHistoryRequest(
    @SerializedName("deviceId")
    val deviceId: String,

    @SerializedName("vodFileId")
    val vodFileId: Long,

    @SerializedName("timestampSeconds")
    val timestampSeconds: Int
)