package com.barrita.android.mainapp.app.data.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("user") val user: UserData?
)

data class UserData(
    @SerializedName("id") val id: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("email_verified") val emailVerified: Boolean?,
    @SerializedName("role_id") val roleId: String?
)
