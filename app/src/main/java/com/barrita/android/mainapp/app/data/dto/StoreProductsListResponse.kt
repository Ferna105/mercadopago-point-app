package com.barrita.android.mainapp.app.data.dto

import com.google.gson.annotations.SerializedName

data class StoreProductsListResponse(
    @SerializedName("store") val store: Store,
    @SerializedName("products") val products: List<Product>
)
