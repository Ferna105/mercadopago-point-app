package com.mercadolibre.android.point_mainapp_demo.app.view.storeproductslist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Product
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppItemProductBinding

class StoreProductsListAdapter(
    private val onItemClick: (Product) -> Unit = {},
    private val onAddToCart: (Product) -> Unit = {}
) : ListAdapter<Product, StoreProductsListAdapter.ProductViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = PointMainappDemoAppItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: PointMainappDemoAppItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.pointMainappDemoAppProductName.text = product.name
            binding.pointMainappDemoAppProductPrice.text = "$${product.price}"
            binding.pointMainappDemoAppProductCategory.text = product.category
            loadProductImage(product.image)
            binding.root.setOnClickListener { onItemClick(product) }
            binding.pointMainappDemoAppProductAddToCart.setOnClickListener {
                it.isClickable = false
                onAddToCart(product)
                it.post { it.isClickable = true }
            }
        }

        private fun loadProductImage(imageRef: String) {
            val imageView = binding.pointMainappDemoAppProductImage
            if (imageRef.isBlank()) {
                imageView.visibility = View.GONE
                return
            }
            imageView.visibility = View.VISIBLE
            if (!imageRef.startsWith("http")) {
                val resId = imageView.context.resources.getIdentifier(
                    imageRef,
                    "drawable",
                    imageView.context.packageName
                )
                if (resId != 0) {
                    imageView.setImageResource(resId)
                } else {
                    imageView.visibility = View.GONE
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem == newItem
    }
}
