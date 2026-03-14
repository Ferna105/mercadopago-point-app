package com.barrita.android.mainapp.app.data.dto

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("observacion_interna") val observacionInterna: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("list_price") val listPrice: Double? = null,
    @SerializedName("price") val priceFromApi: Double? = null,
    @SerializedName("discount") val discount: Double? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("is_available") val isAvailable: Boolean? = null,
    @SerializedName("store_id") val storeId: String? = null
) {
    val isActive: Boolean get() = status == "active" || (isAvailable == true)
    val price: Double get() = listPrice ?: priceFromApi ?: 0.0
}
