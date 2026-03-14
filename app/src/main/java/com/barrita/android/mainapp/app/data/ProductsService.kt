package com.barrita.android.mainapp.app.data

import com.barrita.android.mainapp.app.data.dto.ProductsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ProductsService {
    @GET(NetworkConstants.PRODUCTS_PATH)
    suspend fun getProducts(
        @Header("Authorization") token: String,
        @Query("storeId") storeId: String
    ): Response<ProductsResponse>
}
