package com.barrita.android.mainapp.app.data.dto

import com.google.gson.annotations.SerializedName

data class StoresResponse(
    @SerializedName("statusCode") val statusCode: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<Store>?
)
