package com.barrita.android.mainapp.app.model

import com.barrita.android.mainapp.app.data.NetworkDependencyProvider
import com.barrita.android.mainapp.app.data.RefundsService
import com.barrita.android.mainapp.app.data.dto.RefundResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class RefundsManager(private val refundsClient: RefundsService = NetworkDependencyProvider.refundsService) {
    suspend fun refundPayment(paymentId: Long, accessToken: String): Flow<Response<RefundResponse>> {
        return flow {
            emit(refundsClient.createRefund(paymentId.toString(), accessToken))
        }
    }
}
