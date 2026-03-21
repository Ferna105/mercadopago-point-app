package com.barrita.android.mainapp.app.view.storeproductslist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.cart.CartActivity
import com.barrita.android.mainapp.app.cart.CartRepository
import com.barrita.android.mainapp.app.data.SessionManager
import com.barrita.android.mainapp.app.data.dto.Store
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityStoreProductsListBinding
import com.barrita.android.mainapp.app.view.login.LoginActivity

import com.barrita.android.mainapp.app.util.StoreHoursHelper
import com.barrita.android.mainapp.app.view.storeproductslist.adapter.StoreListBuilder
import com.barrita.android.mainapp.app.view.storeproductslist.adapter.StoreProductsListAdapter
import kotlinx.coroutines.launch
import java.util.Locale

class StoreProductsListActivity : AppCompatActivity() {

    private var binding: PointMainappDemoAppActivityStoreProductsListBinding? = null
    private val viewModel: StoreProductsListViewModel by viewModels()
    private var currentStoreId: String = DEFAULT_STORE_ID
    private val collapsedCategories = mutableSetOf<String>()

    private val adapter = StoreProductsListAdapter(
        onAddToCart = { product ->
            CartRepository.addProduct(product, 1)
        },
        onCategoryToggle = { category ->
            if (collapsedCategories.contains(category)) {
                collapsedCategories.remove(category)
            } else {
                collapsedCategories.add(category)
            }
            rebuildList()
        },
        onSearchQueryChanged = { query ->
            viewModel.setSearchQuery(query)
        },
        onCategorySelected = { category ->
            viewModel.setSelectedCategory(category)
        }
    )

    private var currentStore: Store? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PointMainappDemoAppActivityStoreProductsListBinding.inflate(layoutInflater)
        binding?.run { setContentView(root) }

        val storeId = intent.getStringExtra(EXTRA_STORE_ID) ?: DEFAULT_STORE_ID
        currentStoreId = storeId
        val storeJson = intent.getStringExtra(EXTRA_STORE_JSON)
        val store = storeJson?.let {
            try { Gson().fromJson(it, Store::class.java) } catch (e: Exception) { null }
        }

        setupRecyclerView()
        setupCartBar()
        setupObservers()
        viewModel.loadProducts(storeId, store)
    }

    private fun setupCartBar() {
        binding?.pointMainappDemoAppCartBarCheckout?.setOnClickListener {
            val storeName = currentStore?.name ?: "Tienda"
            startActivity(
                Intent(this, CartActivity::class.java)
                    .putExtra(CartActivity.EXTRA_STORE_NAME, storeName)
                    .putExtra(CartActivity.EXTRA_STORE_ID, currentStoreId)
            )
        }

        binding?.pointMainappDemoAppCartBarTrash?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.point_mainapp_demo_app_cart_clear))
                .setMessage(getString(R.string.point_mainapp_demo_app_cart_clear_confirm))
                .setNegativeButton(getString(R.string.point_mainapp_demo_app_no), null)
                .setPositiveButton(getString(R.string.point_mainapp_demo_app_cart_clear_yes)) { _, _ ->
                    CartRepository.clear()
                }
                .show()
        }

        lifecycleScope.launch {
            CartRepository.items.collect { items ->
                val totalItems = items.sumOf { it.quantity }
                val totalAmount = items.sumOf { it.subtotal }

                if (items.isEmpty()) {
                    binding?.pointMainappDemoAppCartBar?.visibility = View.GONE
                } else {
                    binding?.pointMainappDemoAppCartBar?.visibility = View.VISIBLE
                    binding?.pointMainappDemoAppCartBarCount?.text = totalItems.toString()
                    binding?.pointMainappDemoAppCartBarTotal?.text =
                        String.format(Locale.US, "$%,.0f", totalAmount)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding?.pointMainappDemoAppStoreProductsRecycler?.apply {
            layoutManager = LinearLayoutManager(this@StoreProductsListActivity)
            adapter = this@StoreProductsListActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is StoreProductsListState.Loading -> showLoading()
                is StoreProductsListState.Success -> {
                    currentStore = state.store
                    showSuccess()
                }
                is StoreProductsListState.Error -> showError(state.message)
                is StoreProductsListState.TokenExpired -> redirectToLogin()
            }
        }

        viewModel.allCategoryNames.observe(this) {
            rebuildList()
        }

        viewModel.filteredProducts.observe(this) {
            rebuildList()
        }

        viewModel.selectedCategory.observe(this) {
            rebuildList()
        }
    }

    private fun rebuildList() {
        val store = currentStore ?: return
        val products = viewModel.filteredProducts.value ?: return
        val selectedCat = viewModel.selectedCategory.value
        val categories = viewModel.allCategoryNames.value ?: emptyList()
        val searchQuery = viewModel.searchQuery.value.orEmpty()

        if (products.isEmpty()) {
            showEmpty()
            return
        }

        binding?.pointMainappDemoAppStoreProductsEmpty?.visibility = View.GONE
        binding?.pointMainappDemoAppStoreProductsRecycler?.visibility = View.VISIBLE

        val storeStatus = StoreHoursHelper.isStoreOpen(store.schedule)
        adapter.submitList(
            StoreListBuilder.buildList(
                store, products, selectedCat, collapsedCategories,
                isOpen = storeStatus.isOpen,
                todayHours = storeStatus.todayHours,
                categories = categories,
                searchQuery = searchQuery
            )
        )
    }

    private fun showEmpty() {
        binding?.apply {
            pointMainappDemoAppStoreProductsProgress.visibility = View.GONE
            pointMainappDemoAppStoreProductsError.visibility = View.GONE
            pointMainappDemoAppStoreProductsRecycler.visibility = View.GONE
            pointMainappDemoAppStoreProductsEmpty.visibility = View.VISIBLE

            val query = viewModel.searchQuery.value.orEmpty()
            if (query.isNotEmpty()) {
                pointMainappDemoAppEmptyTitle.text =
                    getString(R.string.point_mainapp_demo_app_empty_no_results)
                pointMainappDemoAppEmptySubtitle.text =
                    getString(R.string.point_mainapp_demo_app_empty_no_results_for, query)
            } else {
                pointMainappDemoAppEmptyTitle.text =
                    getString(R.string.point_mainapp_demo_app_empty_no_products)
                pointMainappDemoAppEmptySubtitle.text =
                    getString(R.string.point_mainapp_demo_app_empty_no_products_sub)
            }
        }
    }

    private fun showLoading() {
        binding?.apply {
            pointMainappDemoAppStoreProductsProgress.visibility = View.VISIBLE
            pointMainappDemoAppStoreProductsRecycler.visibility = View.GONE
            pointMainappDemoAppStoreProductsError.visibility = View.GONE
            pointMainappDemoAppStoreProductsEmpty.visibility = View.GONE
        }
    }

    private fun showSuccess() {
        binding?.apply {
            pointMainappDemoAppStoreProductsProgress.visibility = View.GONE
            pointMainappDemoAppStoreProductsError.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        binding?.apply {
            pointMainappDemoAppStoreProductsProgress.visibility = View.GONE
            pointMainappDemoAppStoreProductsRecycler.visibility = View.GONE
            pointMainappDemoAppStoreProductsEmpty.visibility = View.GONE
            pointMainappDemoAppStoreProductsError.visibility = View.VISIBLE
            pointMainappDemoAppStoreProductsError.text = message
        }
    }

    private fun redirectToLogin() {
        SessionManager.clearSession(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    companion object {
        const val EXTRA_STORE_ID = "extra_store_id"
        const val EXTRA_STORE_JSON = "extra_store_json"
        private const val DEFAULT_STORE_ID = "1"
    }
}
