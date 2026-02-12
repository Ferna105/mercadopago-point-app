package com.mercadolibre.android.point_mainapp_demo.app.view.storeproductslist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Product
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Store
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppItemCategoryHeaderBinding
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppItemProductBinding
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppItemStoreHeaderBinding

class StoreProductsListAdapter(
    private val onItemClick: (Product) -> Unit = {},
    private val onAddToCart: (Product) -> Unit = {}
) : ListAdapter<StoreListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is StoreListItem.StoreHeader -> VIEW_TYPE_STORE_HEADER
        is StoreListItem.CategoryHeader -> VIEW_TYPE_CATEGORY_HEADER
        is StoreListItem.ProductItem -> VIEW_TYPE_PRODUCT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_STORE_HEADER -> {
                val binding = PointMainappDemoAppItemStoreHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                StoreHeaderViewHolder(binding)
            }
            VIEW_TYPE_CATEGORY_HEADER -> {
                val binding = PointMainappDemoAppItemCategoryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CategoryHeaderViewHolder(binding)
            }
            else -> {
                val binding = PointMainappDemoAppItemProductBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ProductViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is StoreListItem.StoreHeader -> (holder as StoreHeaderViewHolder).bind(item.store)
            is StoreListItem.CategoryHeader -> (holder as CategoryHeaderViewHolder).bind(item.category)
            is StoreListItem.ProductItem -> (holder as ProductViewHolder).bind(item.product)
        }
    }

    inner class StoreHeaderViewHolder(
        private val binding: PointMainappDemoAppItemStoreHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(store: Store) {
            binding.pointMainappDemoAppStoreHeaderName.text = store.name
            binding.pointMainappDemoAppStoreHeaderDescription.text = store.description
            loadStoreImage(store.image)
        }

        private fun loadStoreImage(imageRef: String) {
            val imageView = binding.pointMainappDemoAppStoreHeaderImage
            if (imageRef.isBlank()) {
                imageView.visibility = View.GONE
                return
            }
            imageView.visibility = View.VISIBLE
            if (!imageRef.startsWith("http")) {
                val resId = imageView.context.resources.getIdentifier(
                    imageRef, "drawable", imageView.context.packageName
                )
                if (resId != 0) imageView.setImageResource(resId)
                else imageView.visibility = View.GONE
            }
        }
    }

    inner class CategoryHeaderViewHolder(
        private val binding: PointMainappDemoAppItemCategoryHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String) {
            binding.pointMainappDemoAppCategoryHeaderTitle.text = category
        }
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
                    imageRef, "drawable", imageView.context.packageName
                )
                if (resId != 0) imageView.setImageResource(resId)
                else imageView.visibility = View.GONE
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<StoreListItem>() {
        override fun areItemsTheSame(oldItem: StoreListItem, newItem: StoreListItem): Boolean {
            return when {
                oldItem is StoreListItem.StoreHeader && newItem is StoreListItem.StoreHeader ->
                    oldItem.store.id == newItem.store.id
                oldItem is StoreListItem.CategoryHeader && newItem is StoreListItem.CategoryHeader ->
                    oldItem.category == newItem.category
                oldItem is StoreListItem.ProductItem && newItem is StoreListItem.ProductItem ->
                    oldItem.product.id == newItem.product.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: StoreListItem, newItem: StoreListItem): Boolean =
            oldItem == newItem
    }

    companion object {
        private const val VIEW_TYPE_STORE_HEADER = 0
        private const val VIEW_TYPE_CATEGORY_HEADER = 1
        private const val VIEW_TYPE_PRODUCT = 2
    }
}

object StoreListBuilder {
    fun buildList(store: Store, products: List<Product>): List<StoreListItem> {
        val list = mutableListOf<StoreListItem>()
        list.add(StoreListItem.StoreHeader(store))
        val byCategory = products.groupBy { it.category }
        byCategory.keys.sorted().forEach { category ->
            list.add(StoreListItem.CategoryHeader(category))
            byCategory[category]?.forEach { product ->
                list.add(StoreListItem.ProductItem(product))
            }
        }
        return list
    }
}
