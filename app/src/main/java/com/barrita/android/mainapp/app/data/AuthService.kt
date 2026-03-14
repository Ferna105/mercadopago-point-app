package com.barrita.android.mainapp.app.data

import com.barrita.android.mainapp.app.data.dto.LoginRequest
import com.barrita.android.mainapp.app.data.dto.LoginResponse
import com.barrita.android.mainapp.app.data.dto.RefreshRequest
import com.barrita.android.mainapp.app.data.dto.RefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST(NetworkConstants.LOGIN_PATH)
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST(NetworkConstants.REFRESH_PATH)
    suspend fun refreshToken(@Body request: RefreshRequest): Response<RefreshResponse>
}
