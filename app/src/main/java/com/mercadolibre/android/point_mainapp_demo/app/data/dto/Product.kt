package com.mercadolibre.android.point_mainapp_demo.app.data.dto

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("observacion_interna") val observacionInterna: String? = null,
    @SerializedName("price") val price: Double,
    @SerializedName("image") val image: String,
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("category") val category: String,
    @SerializedName("categoryId") val categoryId: Int? = null,
    @SerializedName("categoryIds") val categoryIds: List<Int>? = null,
    @SerializedName("categoryNames") val categoryNames: List<String>? = null,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("storeName") val storeName: String,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("storeIds") val storeIds: List<String>? = null,
    @SerializedName("storeNames") val storeNames: List<String>? = null
)
