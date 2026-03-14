package com.barrita.android.mainapp.app.model

import com.barrita.android.mainapp.app.data.dto.Product
import com.barrita.android.mainapp.app.data.dto.Store
import com.barrita.android.mainapp.app.data.dto.StoreProductsListResponse
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
        logoUrl = "point_mainapp_demo_app_store",
        bannerUrl = null,
        slug = "la-luna-bar",
        phone = null,
        address = null,
        schedule = "Jue a Dom 22:00 - 06:00",
        paymentMethodId = null,
        status = "active"
    )

    private fun createMockProducts(storeId: String, storeName: String): List<Product> = listOf(
        Product(id = "1", name = "Cerveza por chopp", description = "Chopp de cerveza rubia o roja 500 ml", listPrice = 4.50, status = "active", storeId = storeId),
        Product(id = "2", name = "Fernet con Coca", description = "Fernet Branca con Coca-Cola y hielo", listPrice = 5.00, status = "active", storeId = storeId),
        Product(id = "3", name = "Vodka con jugo", description = "Vodka con jugo de naranja o pomelo", listPrice = 6.00, status = "active", storeId = storeId),
        Product(id = "4", name = "Gaseosa 500 ml", description = "Coca-Cola, Sprite o Fanta", listPrice = 2.50, status = "active", storeId = storeId),
        Product(id = "5", name = "Agua mineral", description = "Agua mineral con o sin gas 500 ml", listPrice = 2.00, status = "active", storeId = storeId),
        Product(id = "6", name = "Picada para 2", description = "Jamón, queso, salame, aceitunas y pan", listPrice = 12.00, status = "active", storeId = storeId),
        Product(id = "7", name = "Picada para 4", description = "Picada grande con fiambres, quesos y acompañamientos", listPrice = 22.00, status = "active", storeId = storeId),
        Product(id = "8", name = "Papas fritas", description = "Porción de papas fritas con ketchup y mayo", listPrice = 3.50, status = "active", storeId = storeId),
        Product(id = "9", name = "Nachos con queso", description = "Nachos crocantes con salsa de queso cheddar", listPrice = 4.50, status = "active", storeId = storeId),
        Product(id = "10", name = "Gancia batido", description = "Gancia con pomelo y hielo", listPrice = 5.50, status = "active", storeId = storeId)
    )
}
