package com.barrita.android.mainapp.app.view.storeproductslist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.barrita.android.mainapp.app.cart.CartActivity
import com.barrita.android.mainapp.app.cart.CartRepository
import com.barrita.android.mainapp.app.data.SessionManager
import com.barrita.android.mainapp.app.data.dto.Product
import com.barrita.android.mainapp.app.data.dto.Store
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityStoreProductsListBinding
import com.barrita.android.mainapp.app.view.login.LoginActivity
import com.barrita.android.mainapp.app.view.productdetail.ProductDetailActivity
import com.barrita.android.mainapp.app.view.storeproductslist.adapter.StoreListBuilder
import com.barrita.android.mainapp.app.view.storeproductslist.adapter.StoreProductsListAdapter

class StoreProductsListActivity : AppCompatActivity() {

    private var binding: PointMainappDemoAppActivityStoreProductsListBinding? = null
    private val viewModel: StoreProductsListViewModel by viewModels()
    private val adapter = StoreProductsListAdapter(
        onItemClick = { product ->
            startActivity(
                Intent(this, ProductDetailActivity::class.java)
                    .putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
            )
        },
        onAddToCart = { product ->
            CartRepository.addProduct(product, 1)
            binding?.root?.let { root ->
                Snackbar.make(root, "${product.name} agregado al carrito", Snackbar.LENGTH_SHORT).show()
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PointMainappDemoAppActivityStoreProductsListBinding.inflate(layoutInflater)
        binding?.run { setContentView(root) }

        val storeId = intent.getStringExtra(EXTRA_STORE_ID) ?: DEFAULT_STORE_ID
        val storeJson = intent.getStringExtra(EXTRA_STORE_JSON)
        val store = storeJson?.let {
            try { Gson().fromJson(it, Store::class.java) } catch (e: Exception) { null }
        }

        setupRecyclerView()
        setupCartFab()
        setupObservers()
        viewModel.loadProducts(storeId, store)
    }

    private fun setupCartFab() {
        binding?.pointMainappDemoAppStoreProductsCartFab?.setOnClickListener {
            val storeName = (viewModel.state.value as? StoreProductsListState.Success)?.store?.name ?: "Tienda"
            startActivity(
                Intent(this, CartActivity::class.java).putExtra(CartActivity.EXTRA_STORE_NAME, storeName)
            )
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
                is StoreProductsListState.Success -> showList(state.store, state.products)
                is StoreProductsListState.Error -> showError(state.message)
                is StoreProductsListState.TokenExpired -> redirectToLogin()
            }
        }
    }

    private fun redirectToLogin() {
        SessionManager.clearSession(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showLoading() {
        binding?.apply {
            pointMainappDemoAppStoreProductsProgress.visibility = View.VISIBLE
            pointMainappDemoAppStoreProductsRecycler.visibility = View.GONE
            pointMainappDemoAppStoreProductsError.visibility = View.GONE
        }
    }

    private fun showList(store: Store, products: List<Product>) {
        binding?.apply {
            pointMainappDemoAppStoreProductsProgress.visibility = View.GONE
            pointMainappDemoAppStoreProductsError.visibility = View.GONE
            pointMainappDemoAppStoreProductsRecycler.visibility = View.VISIBLE
        }
        adapter.submitList(StoreListBuilder.buildList(store, products))
    }

    private fun showError(message: String) {
        binding?.apply {
            pointMainappDemoAppStoreProductsProgress.visibility = View.GONE
            pointMainappDemoAppStoreProductsRecycler.visibility = View.GONE
            pointMainappDemoAppStoreProductsError.visibility = View.VISIBLE
            pointMainappDemoAppStoreProductsError.text = message
        }
    }

    companion object {
        const val EXTRA_STORE_ID = "extra_store_id"
        const val EXTRA_STORE_JSON = "extra_store_json"
        private const val DEFAULT_STORE_ID = "1"
    }
}
