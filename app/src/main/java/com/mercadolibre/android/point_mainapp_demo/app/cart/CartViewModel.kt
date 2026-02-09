package com.mercadolibre.android.point_mainapp_demo.app.cart

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class CartViewModel : ViewModel() {

    val items: StateFlow<List<CartItem>> = CartRepository.items

    val totalAmount: Double
        get() = CartRepository.totalAmount

    fun addQuantity(productId: String) {
        val item = CartRepository.items.value.find { it.product.id == productId } ?: return
        CartRepository.updateQuantity(productId, item.quantity + 1)
    }

    fun subtractQuantity(productId: String) {
        val item = CartRepository.items.value.find { it.product.id == productId } ?: return
        CartRepository.updateQuantity(productId, item.quantity - 1)
    }

    fun removeItem(productId: String) {
        CartRepository.removeByProductId(productId)
    }

    fun clearCart() {
        CartRepository.clear()
    }
}
