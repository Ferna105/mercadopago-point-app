package com.barrita.android.mainapp.app.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppItemCartBinding
import com.barrita.android.mainapp.app.util.ImageLoader
import java.util.Locale

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
            val ctx = binding.root.context

            binding.pointMainappDemoAppCartItemName.text = product.name

            val unitPrice = formatPrice(product.finalPrice)
            binding.pointMainappDemoAppCartItemUnitPrice.text =
                ctx.getString(R.string.point_mainapp_demo_app_cart_unit_price, unitPrice)

            binding.pointMainappDemoAppCartItemQuantity.text = item.quantity.toString()

            binding.pointMainappDemoAppCartItemSubtotal.text =
                formatPrice(product.finalPrice * item.quantity)

            loadProductImage(product.imageUrl, product.images)

            binding.pointMainappDemoAppCartItemPlus.setOnClickListener { onAddQuantity(product.id) }
            binding.pointMainappDemoAppCartItemMinus.setOnClickListener {
                if (item.quantity <= 1) {
                    onRemove(product.id)
                } else {
                    onSubtractQuantity(product.id)
                }
            }
        }

        private fun loadProductImage(imageUrl: String?, images: List<String>?) {
            val url = images?.firstOrNull() ?: imageUrl
            if (url.isNullOrBlank()) {
                binding.pointMainappDemoAppCartItemImage.visibility = View.GONE
                binding.pointMainappDemoAppCartItemPlaceholder.visibility = View.VISIBLE
                return
            }
            binding.pointMainappDemoAppCartItemPlaceholder.visibility = View.GONE
            binding.pointMainappDemoAppCartItemImage.visibility = View.VISIBLE
            if (url.startsWith("http")) {
                ImageLoader.load(binding.pointMainappDemoAppCartItemImage, url)
            } else {
                val resId = binding.root.context.resources.getIdentifier(
                    url, "drawable", binding.root.context.packageName
                )
                if (resId != 0) {
                    binding.pointMainappDemoAppCartItemImage.setImageResource(resId)
                } else {
                    binding.pointMainappDemoAppCartItemImage.visibility = View.GONE
                    binding.pointMainappDemoAppCartItemPlaceholder.visibility = View.VISIBLE
                }
            }
        }

        private fun formatPrice(price: Double): String =
            String.format(Locale.US, "$%,.0f", price)
    }

    private class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem.product.id == newItem.product.id

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem == newItem
    }
}
