package com.mercadolibre.android.point_mainapp_demo.app.cart

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mercadolibre.android.point_mainapp_demo.app.R
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppActivityCartBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {

    private var binding: PointMainappDemoAppActivityCartBinding? = null
    private val viewModel: CartViewModel by viewModels()
    private val adapter = CartAdapter(
        onAddQuantity = { viewModel.addQuantity(it) },
        onSubtractQuantity = { viewModel.subtractQuantity(it) },
        onRemove = { viewModel.removeItem(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PointMainappDemoAppActivityCartBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupToolbar()
        setupRecyclerView()
        observeCart()
    }

    private fun setupToolbar() {
        binding?.pointMainappDemoAppCartToolbar?.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        binding?.pointMainappDemoAppCartRecycler?.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            this.adapter = this@CartActivity.adapter
        }
    }

    private fun observeCart() {
        lifecycleScope.launchWhenStarted {
            viewModel.items.collect { items ->
                if (items.isEmpty()) {
                    binding?.apply {
                        pointMainappDemoAppCartEmpty.visibility = View.VISIBLE
                        pointMainappDemoAppCartRecycler.visibility = View.GONE
                        pointMainappDemoAppCartTotalContainer.visibility = View.GONE
                    }
                } else {
                    binding?.apply {
                        pointMainappDemoAppCartEmpty.visibility = View.GONE
                        pointMainappDemoAppCartRecycler.visibility = View.VISIBLE
                        pointMainappDemoAppCartTotalContainer.visibility = View.VISIBLE
                        pointMainappDemoAppCartTotalLabel.text =
                            getString(R.string.point_mainapp_demo_app_cart_total, String.format("$%.2f", viewModel.totalAmount))
                    }
                    adapter.submitList(items)
                }
            }
        }
    }
}
