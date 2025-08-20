package com.example.newez.model

import com.google.gson.annotations.SerializedName

data class IpUpdateRequest(
    @SerializedName("ip")
    val ip: String
)