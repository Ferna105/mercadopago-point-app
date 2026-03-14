package com.barrita.android.mainapp.app.model

import com.barrita.android.mainapp.app.data.dto.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Returns mock product detail for the given product id. No real network call.
 */
class ProductDetailManager {

    fun fetchProductDetail(productId: String): Flow<Product> {
        return flow {
            delay(400)
            emit(createMockProduct(productId))
        }
    }

    private fun createMockProduct(productId: String): Product = Product(
        id = productId,
        name = "Café Americano",
        description = "Café negro recién preparado. Ideal para empezar el día.",
        observacionInterna = null,
        imageUrl = null,
        listPrice = 2.50,
        discount = null,
        status = "active",
        storeId = "1"
    )
}
