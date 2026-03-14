package com.barrita.android.mainapp.app.data.dto

import com.google.gson.annotations.SerializedName

data class Store(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("logo_url") val logoUrl: String? = null,
    @SerializedName("banner_url") val bannerUrl: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("schedule") val schedule: String? = null,
    @SerializedName("payment_method_id") val paymentMethodId: String? = null,
    @SerializedName("status") val status: String? = null
)
