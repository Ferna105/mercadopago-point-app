package com.mercadolibre.android.point_mainapp_demo.app.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppItemCartBinding

class CartAdapter(
    private val onAddQuantity: (String) -> Unit = {},
    private val onSubtractQuantity: (String) -> Unit = {},
    private val onRemove: (String) -> Unit = {}
) : ListAdapter<CartItem, CartAdapter.CartItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = PointMainappDemoAppItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartItemViewHolder(
        private val binding: PointMainappDemoAppItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            val product = item.product
            binding.pointMainappDemoAppCartItemName.text = product.name
            binding.pointMainappDemoAppCartItemPrice.text = "$${product.price} c/u"
            binding.pointMainappDemoAppCartItemQuantity.text = item.quantity.toString()
            binding.pointMainappDemoAppCartItemSubtotal.text = "$${String.format("%.2f", item.subtotal)}"
            binding.pointMainappDemoAppCartItemPlus.setOnClickListener { onAddQuantity(product.id) }
            binding.pointMainappDemoAppCartItemMinus.setOnClickListener { onSubtractQuantity(product.id) }
            binding.pointMainappDemoAppCartItemRemove.setOnClickListener { onRemove(product.id) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem.product.id == newItem.product.id

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem == newItem
    }
}
