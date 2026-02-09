package com.mercadolibre.android.point_mainapp_demo.app.model

import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Product
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
        price = 2.50,
        image = "",
        images = emptyList(),
        category = "Bebidas",
        categoryId = 1,
        categoryIds = listOf(1),
        categoryNames = listOf("Bebidas"),
        isActive = true,
        storeName = "Store 1",
        storeId = "1",
        storeIds = listOf("1"),
        storeNames = listOf("Store 1")
    )
}
