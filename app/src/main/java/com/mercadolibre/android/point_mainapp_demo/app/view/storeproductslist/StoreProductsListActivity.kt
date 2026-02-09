package com.mercadolibre.android.point_mainapp_demo.app.view.storeproductslist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Product
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Store
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppActivityStoreProductsListBinding
import com.mercadolibre.android.point_mainapp_demo.app.view.productdetail.ProductDetailActivity
import com.mercadolibre.android.point_mainapp_demo.app.view.storeproductslist.adapter.StoreProductsListAdapter

class StoreProductsListActivity : AppCompatActivity() {

    private var binding: PointMainappDemoAppActivityStoreProductsListBinding? = null
    private val viewModel: StoreProductsListViewModel by viewModels()
    private val adapter = StoreProductsListAdapter { product ->
        startActivity(
            Intent(this, ProductDetailActivity::class.java)
                .putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PointMainappDemoAppActivityStoreProductsListBinding.inflate(layoutInflater)
        binding?.run { setContentView(root) }
        val storeId = intent.getStringExtra(EXTRA_STORE_ID) ?: DEFAULT_STORE_ID
        setupRecyclerView()
        setupObservers()
        viewModel.loadProducts(storeId)
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
            }
        }
    }

    private fun showLoading() {
        binding?.apply {
            pointMainappDemoAppStoreProductsProgress.visibility = View.VISIBLE
            pointMainappDemoAppStoreProductsRecycler.visibility = View.GONE
            pointMainappDemoAppStoreInfoContainer.visibility = View.GONE
            pointMainappDemoAppStoreProductsError.visibility = View.GONE
        }
    }

    private fun showList(store: Store, products: List<Product>) {
        binding?.apply {
            pointMainappDemoAppStoreProductsProgress.visibility = View.GONE
            pointMainappDemoAppStoreProductsError.visibility = View.GONE
            pointMainappDemoAppStoreInfoContainer.visibility = View.VISIBLE
            pointMainappDemoAppStoreProductsRecycler.visibility = View.VISIBLE
            pointMainappDemoAppStoreInfoName.text = store.name
            pointMainappDemoAppStoreInfoDescription.text = store.description
        }
        adapter.submitList(products)
    }

    private fun showError(message: String) {
        binding?.apply {
            pointMainappDemoAppStoreProductsProgress.visibility = View.GONE
            pointMainappDemoAppStoreProductsRecycler.visibility = View.GONE
            pointMainappDemoAppStoreInfoContainer.visibility = View.GONE
            pointMainappDemoAppStoreProductsError.visibility = View.VISIBLE
            pointMainappDemoAppStoreProductsError.text = message
        }
    }

    companion object {
        const val EXTRA_STORE_ID = "extra_store_id"
        private const val DEFAULT_STORE_ID = "1"
    }
}
