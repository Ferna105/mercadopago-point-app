package com.barrita.android.mainapp.app.data

import com.barrita.android.mainapp.app.data.dto.ConfirmPaymentRequest
import com.barrita.android.mainapp.app.data.dto.CreateOrderRequest
import com.barrita.android.mainapp.app.data.dto.CreateOrderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OrdersService {
    @POST(NetworkConstants.MENU_ORDERS_PATH)
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): Response<CreateOrderResponse>

    @POST(NetworkConstants.PAYMENT_CONFIRM_PATH)
    suspend fun confirmPayment(
        @Body request: ConfirmPaymentRequest
    ): Response<Unit>
}
