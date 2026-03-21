package com.barrita.android.mainapp.app.view.storeproductslist.adapter

import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.data.dto.Product
import com.barrita.android.mainapp.app.util.ImageLoader
import com.barrita.android.mainapp.app.data.dto.Store
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppItemCategoryHeaderBinding
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppItemCategoryTabsBinding
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppItemClosedBannerBinding
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppItemProductBinding
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppItemStoreHeaderBinding
import java.util.Locale

class StoreProductsListAdapter(
    private val onAddToCart: (Product) -> Unit = {},
    private val onCategoryToggle: (String) -> Unit = {},
    private val onSearchQueryChanged: (String) -> Unit = {},
    private val onCategorySelected: (String?) -> Unit = {}
) : ListAdapter<StoreListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is StoreListItem.StoreHeader -> VIEW_TYPE_STORE_HEADER
        is StoreListItem.ClosedBanner -> VIEW_TYPE_CLOSED_BANNER
        is StoreListItem.CategoryTabs -> VIEW_TYPE_CATEGORY_TABS
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
            VIEW_TYPE_CLOSED_BANNER -> {
                val binding = PointMainappDemoAppItemClosedBannerBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ClosedBannerViewHolder(binding)
            }
            VIEW_TYPE_CATEGORY_TABS -> {
                val binding = PointMainappDemoAppItemCategoryTabsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CategoryTabsViewHolder(binding)
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
            is StoreListItem.StoreHeader -> (holder as StoreHeaderViewHolder).bind(item)
            is StoreListItem.ClosedBanner -> (holder as ClosedBannerViewHolder).bind(item)
            is StoreListItem.CategoryTabs -> (holder as CategoryTabsViewHolder).bind(item)
            is StoreListItem.CategoryHeader -> (holder as CategoryHeaderViewHolder).bind(item)
            is StoreListItem.ProductItem -> (holder as ProductViewHolder).bind(item.product)
        }
    }

    inner class ClosedBannerViewHolder(
        private val binding: PointMainappDemoAppItemClosedBannerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StoreListItem.ClosedBanner) {
            val ctx = binding.root.context
            binding.pointMainappDemoAppClosedBannerHours.text =
                if (item.todayHours != null)
                    ctx.getString(R.string.point_mainapp_demo_app_store_closed_hours, item.todayHours)
                else
                    ctx.getString(R.string.point_mainapp_demo_app_store_closed)
        }
    }

    inner class StoreHeaderViewHolder(
        private val binding: PointMainappDemoAppItemStoreHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StoreListItem.StoreHeader) {
            val store = item.store
            binding.pointMainappDemoAppStoreHeaderName.text = store.name
            val desc = store.description
            if (!desc.isNullOrBlank() && !desc.contains("tienda creada por", ignoreCase = true)) {
                binding.pointMainappDemoAppStoreHeaderDescription.text = desc
                binding.pointMainappDemoAppStoreHeaderDescription.visibility = View.VISIBLE
            } else {
                binding.pointMainappDemoAppStoreHeaderDescription.visibility = View.GONE
            }
            binding.pointMainappDemoAppStoreHeaderMeta.text = "Mercado Pago"

            if (item.isOpen) {
                binding.pointMainappDemoAppStoreHeaderBadge.visibility = View.VISIBLE
                binding.pointMainappDemoAppStoreHeaderBadge.text =
                    binding.root.context.getString(R.string.point_mainapp_demo_app_store_open)
            } else {
                binding.pointMainappDemoAppStoreHeaderBadge.visibility = View.GONE
            }

            loadStoreImage(store.logoUrl, store.name)
        }

        private fun loadStoreImage(imageRef: String?, name: String) {
            val imageView = binding.pointMainappDemoAppStoreHeaderImage
            val initialsView = binding.pointMainappDemoAppStoreHeaderInitials
            if (imageRef.isNullOrBlank()) {
                imageView.visibility = View.GONE
                initialsView.visibility = View.VISIBLE
                initialsView.text = getInitials(name)
                return
            }
            initialsView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            if (imageRef.startsWith("http")) {
                ImageLoader.load(imageView, imageRef)
            } else {
                val resId = imageView.context.resources.getIdentifier(
                    imageRef, "drawable", imageView.context.packageName
                )
                if (resId != 0) imageView.setImageResource(resId)
                else {
                    imageView.visibility = View.GONE
                    initialsView.visibility = View.VISIBLE
                    initialsView.text = getInitials(name)
                }
            }
        }

        private fun getInitials(name: String): String =
            name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
    }

    inner class CategoryTabsViewHolder(
        private val binding: PointMainappDemoAppItemCategoryTabsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val tabViews = mutableListOf<TextView>()
        private var textWatcher: TextWatcher? = null

        fun bind(item: StoreListItem.CategoryTabs) {
            val ctx = binding.root.context

            val searchInput = binding.pointMainappDemoAppTabSearchInput
            val searchClear = binding.pointMainappDemoAppTabSearchClear

            textWatcher?.let { searchInput.removeTextChangedListener(it) }

            if (item.searchQuery.isNotEmpty()) {
                searchInput.setText(item.searchQuery)
                searchInput.setSelection(item.searchQuery.length)
                searchClear.visibility = View.VISIBLE
            } else {
                searchInput.setText("")
                searchClear.visibility = View.GONE
            }

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val q = s?.toString().orEmpty()
                    searchClear.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                    onSearchQueryChanged(q)
                }
            }
            textWatcher = watcher
            searchInput.addTextChangedListener(watcher)

            searchClear.setOnClickListener {
                searchInput.setText("")
            }

            val container = binding.pointMainappDemoAppTabContainer
            container.removeAllViews()
            tabViews.clear()

            if (item.categories.isEmpty()) {
                binding.pointMainappDemoAppTabScroll.visibility = View.GONE
                binding.pointMainappDemoAppTabDivider.visibility = View.GONE
                return
            }

            binding.pointMainappDemoAppTabScroll.visibility = View.VISIBLE
            binding.pointMainappDemoAppTabDivider.visibility = View.VISIBLE

            val allItems = listOf<String?>(null) + item.categories
            val tealColor = ContextCompat.getColor(ctx, R.color.barrita_teal)
            val grayColor = ContextCompat.getColor(ctx, R.color.barrita_text_secondary)

            allItems.forEach { cat ->
                val label = cat ?: ctx.getString(R.string.point_mainapp_demo_app_category_all)
                val tv = TextView(ctx)
                tv.text = label
                tv.textSize = 14f
                tv.setTypeface(null, Typeface.BOLD)
                tv.gravity = Gravity.CENTER
                val density = ctx.resources.displayMetrics.density
                val hPad = (14 * density).toInt()
                val bPad = (2 * density).toInt()
                tv.setPadding(hPad, 0, hPad, bPad)
                tv.tag = cat

                val isSelected = cat == item.selectedCategory
                tv.setTextColor(if (isSelected) tealColor else grayColor)

                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                tv.layoutParams = lp

                tv.setOnClickListener { onCategorySelected(cat) }
                tabViews.add(tv)
                container.addView(tv)
            }
        }
    }

    inner class CategoryHeaderViewHolder(
        private val binding: PointMainappDemoAppItemCategoryHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StoreListItem.CategoryHeader) {
            binding.pointMainappDemoAppCategoryHeaderTitle.text = item.category
            val count = item.productCount
            binding.pointMainappDemoAppCategoryHeaderCount.text = if (count == 1) "1 producto" else "$count productos"
            binding.pointMainappDemoAppCategoryHeaderChevron.rotation = if (item.expanded) 0f else 180f
            binding.root.setOnClickListener { onCategoryToggle(item.category) }
        }
    }

    inner class ProductViewHolder(
        private val binding: PointMainappDemoAppItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.pointMainappDemoAppProductName.text = product.name

            val desc = product.description
            if (!desc.isNullOrBlank()) {
                binding.pointMainappDemoAppProductDescription.text = desc
                binding.pointMainappDemoAppProductDescription.visibility = View.VISIBLE
            } else {
                binding.pointMainappDemoAppProductDescription.visibility = View.GONE
            }

            binding.pointMainappDemoAppProductPrice.text =
                String.format(Locale.US, "$%,.0f", product.finalPrice)

            loadProductImage(product.imageUrl)
            binding.pointMainappDemoAppProductAddToCart.setOnClickListener {
                it.isClickable = false
                onAddToCart(product)
                it.post { it.isClickable = true }
            }
        }

        private fun loadProductImage(imageRef: String?) {
            val imageView = binding.pointMainappDemoAppProductImage
            if (imageRef.isNullOrBlank()) {
                imageView.visibility = View.GONE
                return
            }
            imageView.visibility = View.VISIBLE
            if (imageRef.startsWith("http")) {
                ImageLoader.load(imageView, imageRef)
            } else {
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
                oldItem is StoreListItem.ClosedBanner && newItem is StoreListItem.ClosedBanner -> true
                oldItem is StoreListItem.CategoryTabs && newItem is StoreListItem.CategoryTabs -> true
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
        private const val VIEW_TYPE_CLOSED_BANNER = 1
        private const val VIEW_TYPE_CATEGORY_HEADER = 2
        private const val VIEW_TYPE_PRODUCT = 3
        private const val VIEW_TYPE_CATEGORY_TABS = 4
    }
}

sealed class StoreListItem {
    data class StoreHeader(
        val store: Store,
        val isOpen: Boolean = true
    ) : StoreListItem()

    data class ClosedBanner(val todayHours: String?) : StoreListItem()

    data class CategoryTabs(
        val categories: List<String>,
        val selectedCategory: String?,
        val searchQuery: String
    ) : StoreListItem()

    data class CategoryHeader(
        val category: String,
        val productCount: Int = 0,
        val expanded: Boolean = true
    ) : StoreListItem()

    data class ProductItem(val product: Product) : StoreListItem()
}

object StoreListBuilder {
    fun buildList(
        store: Store,
        products: List<Product>,
        selectedCategory: String?,
        collapsedCategories: Set<String> = emptySet(),
        isOpen: Boolean = true,
        todayHours: String? = null,
        categories: List<String> = emptyList(),
        searchQuery: String = ""
    ): List<StoreListItem> {
        val list = mutableListOf<StoreListItem>()
        list.add(StoreListItem.StoreHeader(store, isOpen))

        if (!isOpen) {
            list.add(StoreListItem.ClosedBanner(todayHours))
        }

        list.add(StoreListItem.CategoryTabs(categories, selectedCategory, searchQuery))

        if (products.isEmpty()) return list

        val productsByCategory = linkedMapOf<String, MutableList<Product>>()
        val uncategorized = mutableListOf<Product>()

        products.forEach { product ->
            val cats = product.resolvedCategories
            if (cats.isEmpty()) {
                uncategorized.add(product)
            } else {
                cats.forEach { catName ->
                    if (selectedCategory != null && catName != selectedCategory) return@forEach
                    productsByCategory.getOrPut(catName) { mutableListOf() }.add(product)
                }
            }
        }

        val sortedCategories = productsByCategory.entries.sortedBy { it.key }

        sortedCategories.forEach { (catName, catProducts) ->
            val expanded = catName !in collapsedCategories
            list.add(StoreListItem.CategoryHeader(catName, catProducts.size, expanded))
            if (expanded) {
                catProducts.forEach { list.add(StoreListItem.ProductItem(it)) }
            }
        }

        if (uncategorized.isNotEmpty()) {
            val expanded = "Otros" !in collapsedCategories
            list.add(StoreListItem.CategoryHeader("Otros", uncategorized.size, expanded))
            if (expanded) {
                uncategorized.forEach { list.add(StoreListItem.ProductItem(it)) }
            }
        }

        return list
    }
}
