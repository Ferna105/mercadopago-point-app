package com.barrita.android.mainapp.app.data

import com.barrita.android.mainapp.app.data.dto.StoresResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface StoresService {
    @GET(NetworkConstants.STORES_PATH)
    suspend fun getStores(
        @Header("Authorization") token: String
    ): Response<StoresResponse>
}
