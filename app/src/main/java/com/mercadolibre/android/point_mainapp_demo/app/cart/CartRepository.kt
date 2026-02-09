package com.mercadolibre.android.point_mainapp_demo.app.cart

import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Double get() = product.price * quantity
}

/**
 * Repositorio del carrito en memoria. Singleton para compartir estado entre listado y pantalla de carrito.
 */
object CartRepository {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    val totalItems: Int
        get() = _items.value.sumOf { it.quantity }

    val totalAmount: Double
        get() = _items.value.sumOf { it.subtotal }

    fun addProduct(product: Product, quantity: Int = 1) {
        val current = _items.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity))
        }
        _items.value = current
    }

    fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeByProductId(productId)
            return
        }
        val current = _items.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = quantity)
            _items.value = current
        }
    }

    fun removeByProductId(productId: String) {
        _items.value = _items.value.filter { it.product.id != productId }
    }

    fun clear() {
        _items.value = emptyList()
    }
}
