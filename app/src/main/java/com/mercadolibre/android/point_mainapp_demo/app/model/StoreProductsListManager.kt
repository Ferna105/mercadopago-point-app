package com.mercadolibre.android.point_mainapp_demo.app.model

import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Product
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Store
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.StoreProductsListResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Returns mock store and products for the given store id. No real network call.
 */
class StoreProductsListManager {

    fun fetchStoreProducts(storeId: String): Flow<StoreProductsListResponse> {
        return flow {
            delay(500) // Simulate network delay
            val store = createMockStore(storeId)
            val products = createMockProducts(storeId, store.name)
            emit(StoreProductsListResponse(store = store, products = products))
        }
    }

    private fun createMockStore(storeId: String): Store = Store(
        id = storeId,
        name = "Cafetería Central",
        description = "Café de especialidad y pastelería artesanal en el centro.",
        image = "",
        bannerUrl = null,
        schedule = "Lun a Vie 8:00 - 20:00, Sáb 9:00 - 14:00",
        paymentMethod = "Efectivo y tarjetas",
        paymentMethodId = null,
        productsCount = 5,
        status = "active"
    )

    private fun createMockProducts(storeId: String, storeName: String): List<Product> = listOf(
        Product(
            id = "1",
            name = "Café Americano",
            description = "Café negro recién preparado",
            price = 2.50,
            image = "",
            images = emptyList(),
            category = "Bebidas",
            categoryNames = listOf("Bebidas"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "2",
            name = "Medialuna",
            description = "Medialuna de manteca",
            price = 1.80,
            image = "",
            images = emptyList(),
            category = "Pastelería",
            categoryNames = listOf("Pastelería"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "3",
            name = "Sandwich de jamón y queso",
            description = "Sandwich con jamón cocido y queso",
            price = 4.20,
            image = "",
            images = emptyList(),
            category = "Sandwiches",
            categoryNames = listOf("Sandwiches"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "4",
            name = "Jugo de naranja",
            description = "Jugo exprimido natural",
            price = 3.00,
            image = "",
            images = emptyList(),
            category = "Bebidas",
            categoryNames = listOf("Bebidas"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "5",
            name = "Tostado",
            description = "Tostado de jamón y queso",
            price = 3.50,
            image = "",
            images = emptyList(),
            category = "Sandwiches",
            categoryNames = listOf("Sandwiches"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        )
    )
}
