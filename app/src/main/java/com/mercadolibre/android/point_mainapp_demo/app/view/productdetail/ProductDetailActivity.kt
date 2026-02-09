package com.mercadolibre.android.point_mainapp_demo.app.view.productdetail

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mercadolibre.android.point_mainapp_demo.app.R
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Product
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {

    private var binding: PointMainappDemoAppActivityProductDetailBinding? = null
    private val viewModel: ProductDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PointMainappDemoAppActivityProductDetailBinding.inflate(layoutInflater)
        binding?.run { setContentView(root) }
        val productId = intent.getStringExtra(EXTRA_PRODUCT_ID) ?: run {
            finish()
            return
        }
        setupObservers()
        viewModel.loadDetail(productId)
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is ProductDetailState.Loading -> showLoading()
                is ProductDetailState.Success -> showDetail(state.product)
                is ProductDetailState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        binding?.apply {
            pointMainappDemoAppProductDetailProgress.visibility = View.VISIBLE
            pointMainappDemoAppProductDetailContent.visibility = View.GONE
            pointMainappDemoAppProductDetailError.visibility = View.GONE
        }
    }

    private fun showDetail(product: Product) {
        binding?.apply {
            pointMainappDemoAppProductDetailProgress.visibility = View.GONE
            pointMainappDemoAppProductDetailError.visibility = View.GONE
            pointMainappDemoAppProductDetailContent.visibility = View.VISIBLE

            pointMainappDemoAppProductDetailName.text = product.name
            pointMainappDemoAppProductDetailId.text = getString(R.string.point_mainapp_demo_app_product_detail_id_format, product.id)
            pointMainappDemoAppProductDetailPrice.text = "$${product.price}"
            pointMainappDemoAppProductDetailCategory.text = getString(R.string.point_mainapp_demo_app_product_detail_category_format, product.category)
            pointMainappDemoAppProductDetailStore.text = getString(R.string.point_mainapp_demo_app_product_detail_store_format, product.storeName)
            pointMainappDemoAppProductDetailDescription.text = product.description
            pointMainappDemoAppProductDetailActive.text = getString(
                R.string.point_mainapp_demo_app_product_detail_active_format,
                if (product.isActive) getString(R.string.point_mainapp_demo_app_yes) else getString(R.string.point_mainapp_demo_app_no)
            )
        }
    }

    private fun showError(message: String) {
        binding?.apply {
            pointMainappDemoAppProductDetailProgress.visibility = View.GONE
            pointMainappDemoAppProductDetailContent.visibility = View.GONE
            pointMainappDemoAppProductDetailError.visibility = View.VISIBLE
            pointMainappDemoAppProductDetailError.text = message
        }
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }
}
