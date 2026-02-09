package com.mercadolibre.android.point_mainapp_demo.app.data.dto

import com.google.gson.annotations.SerializedName

data class Store(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("image") val image: String,
    @SerializedName("banner_url") val bannerUrl: String? = null,
    @SerializedName("schedule") val schedule: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("payment_method_id") val paymentMethodId: Int? = null,
    @SerializedName("productsCount") val productsCount: Int,
    @SerializedName("status") val status: String // "active" | "inactive"
)
