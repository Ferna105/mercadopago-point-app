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
        name = "La Luna Bar",
        description = "Bar y boliche con barra completa, música en vivo los fines de semana y patio exterior.",
        image = "point_mainapp_demo_app_store",
        bannerUrl = null,
        schedule = "Jue a Dom 22:00 - 06:00",
        paymentMethod = "Efectivo, tarjetas y transferencia",
        paymentMethodId = null,
        productsCount = 10,
        status = "active"
    )

    private fun createMockProducts(storeId: String, storeName: String): List<Product> = listOf(
        Product(
            id = "1",
            name = "Cerveza por chopp",
            description = "Chopp de cerveza rubia o roja 500 ml",
            price = 4.50,
            image = "point_mainapp_demo_app_store",
            images = emptyList(),
            category = "Bebidas con alcohol",
            categoryNames = listOf("Bebidas con alcohol"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "2",
            name = "Fernet con Coca",
            description = "Fernet Branca con Coca-Cola y hielo",
            price = 5.00,
            image = "point_mainapp_demo_app_store",
            images = emptyList(),
            category = "Bebidas con alcohol",
            categoryNames = listOf("Bebidas con alcohol"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "3",
            name = "Vodka con jugo",
            description = "Vodka con jugo de naranja o pomelo",
            price = 6.00,
            image = "point_mainapp_demo_app_store",
            images = emptyList(),
            category = "Bebidas con alcohol",
            categoryNames = listOf("Bebidas con alcohol"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "4",
            name = "Gaseosa 500 ml",
            description = "Coca-Cola, Sprite o Fanta",
            price = 2.50,
            image = "point_mainapp_demo_app_store",
            images = emptyList(),
            category = "Bebidas sin alcohol",
            categoryNames = listOf("Bebidas sin alcohol"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "5",
            name = "Agua mineral",
            description = "Agua mineral con o sin gas 500 ml",
            price = 2.00,
            image = "point_mainapp_demo_app_store",
            images = emptyList(),
            category = "Bebidas sin alcohol",
            categoryNames = listOf("Bebidas sin alcohol"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "6",
            name = "Picada para 2",
            description = "Jamón, queso, salame, aceitunas y pan",
            price = 12.00,
            image = "point_mainapp_demo_app_store",
            images = emptyList(),
            category = "Picadas",
            categoryNames = listOf("Picadas"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "7",
            name = "Picada para 4",
            description = "Picada grande con fiambres, quesos y acompañamientos",
            price = 22.00,
            image = "point_mainapp_demo_app_store",
            images = emptyList(),
            category = "Picadas",
            categoryNames = listOf("Picadas"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "8",
            name = "Papas fritas",
            description = "Porción de papas fritas con ketchup y mayo",
            price = 3.50,
            image = "point_mainapp_demo_app_store",
            images = emptyList(),
            category = "Snacks",
            categoryNames = listOf("Snacks"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "9",
            name = "Nachos con queso",
            description = "Nachos crocantes con salsa de queso cheddar",
            price = 4.50,
            image = "point_mainapp_demo_app_store",
            images = emptyList(),
            category = "Snacks",
            categoryNames = listOf("Snacks"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        ),
        Product(
            id = "10",
            name = "Gancia batido",
            description = "Gancia con pomelo y hielo",
            price = 5.50,
            image = "point_mainapp_demo_app_store",
            images = emptyList(),
            category = "Bebidas con alcohol",
            categoryNames = listOf("Bebidas con alcohol"),
            isActive = true,
            storeName = storeName,
            storeId = storeId,
            storeIds = listOf(storeId),
            storeNames = listOf(storeName)
        )
    )
}
