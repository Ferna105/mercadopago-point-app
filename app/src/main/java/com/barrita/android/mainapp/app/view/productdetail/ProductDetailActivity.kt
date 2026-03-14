package com.barrita.android.mainapp.app.view.productdetail

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.util.ImageLoader
import com.barrita.android.mainapp.app.data.dto.Product
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityProductDetailBinding

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
            pointMainappDemoAppProductDetailCategory.text = getString(R.string.point_mainapp_demo_app_product_detail_category_format, "-")
            pointMainappDemoAppProductDetailStore.text = getString(R.string.point_mainapp_demo_app_product_detail_store_format, product.storeId ?: "-")
            pointMainappDemoAppProductDetailDescription.text = product.description ?: ""
            pointMainappDemoAppProductDetailActive.text = getString(
                R.string.point_mainapp_demo_app_product_detail_active_format,
                if (product.isActive) getString(R.string.point_mainapp_demo_app_yes) else getString(R.string.point_mainapp_demo_app_no)
            )
            loadProductImage(product.imageUrl)
        }
    }

    private fun loadProductImage(imageRef: String?) {
        val imageView = binding?.pointMainappDemoAppProductDetailImage ?: return
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
            if (resId != 0) {
                imageView.setImageResource(resId)
            } else {
                imageView.visibility = View.GONE
            }
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
