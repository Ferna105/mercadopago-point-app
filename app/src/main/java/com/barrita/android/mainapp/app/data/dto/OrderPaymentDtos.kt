package com.barrita.android.mainapp.app.data.dto

import com.google.gson.annotations.SerializedName

data class CreateOrderRequest(
    @SerializedName("storeId") val storeId: String,
    @SerializedName("qrCode") val qrCode: String?,
    @SerializedName("items") val items: List<CreateOrderItemRequest>,
    @SerializedName("total") val total: Double
)

data class CreateOrderItemRequest(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") val price: Double
)

data class CreateOrderResponse(
    @SerializedName("statusCode") val statusCode: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: CreateOrderData?
)

data class CreateOrderData(
    @SerializedName("orderId") val orderId: String?
)

data class ConfirmPaymentRequest(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("paymentReference") val paymentReference: String? = null,
    @SerializedName("transactionId") val transactionId: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("method") val method: String? = null,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("lastFour") val lastFour: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("tip") val tip: Double? = null,
    @SerializedName("errorMessage") val errorMessage: String? = null,
    @SerializedName("sourceChannel") val sourceChannel: String = "point_app"
)
