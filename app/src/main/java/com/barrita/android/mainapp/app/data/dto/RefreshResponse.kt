package com.barrita.android.mainapp.app.data.dto

import com.google.gson.annotations.SerializedName

data class RefreshResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?
)
