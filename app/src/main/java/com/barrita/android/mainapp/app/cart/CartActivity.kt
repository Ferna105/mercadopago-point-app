package com.barrita.android.mainapp.app.cart

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityCartBinding
import com.google.android.material.snackbar.Snackbar
import com.mercadolibre.android.point_integration_sdk.nativesdk.MPManager
import com.mercadolibre.android.point_integration_sdk.nativesdk.message.utils.doIfError
import com.mercadolibre.android.point_integration_sdk.nativesdk.message.utils.doIfSuccess
import com.mercadolibre.android.point_integration_sdk.nativesdk.payment.data.PaymentFlowRequestData
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private var binding: PointMainappDemoAppActivityCartBinding? = null
    private val viewModel: CartViewModel by viewModels()
    private val adapter = CartAdapter(
        onAddQuantity = { viewModel.addQuantity(it) },
        onSubtractQuantity = { viewModel.subtractQuantity(it) },
        onRemove = { viewModel.removeItem(it) }
    )

    private val storeName: String
        get() = intent.getStringExtra(EXTRA_STORE_NAME) ?: "Tienda"

    private val paymentFlow = MPManager.paymentFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PointMainappDemoAppActivityCartBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupToolbar()
        setupRecyclerView()
        setupGoToPayButton()
        observeCart()
    }

    private fun setupGoToPayButton() {
        binding?.pointMainappDemoAppCartGoToPay?.setOnClickListener {
            val total = viewModel.totalAmount
            val amountStr = String.format(Locale.US, "%.2f", total)
            val description = getString(R.string.point_mainapp_demo_app_cart_payment_description, storeName)
            binding?.pointMainappDemoAppCartGoToPay?.isEnabled = false
            paymentFlow.launchPaymentFlow(
                PaymentFlowRequestData(
                    amount = amountStr.toDouble(),
                    description = description,
                    paymentMethod = null,
                    printOnTerminal = true,
                    taxes = null
                )
            ) { response ->
                binding?.pointMainappDemoAppCartGoToPay?.isEnabled = true
                response.doIfSuccess {
                    binding?.root?.let { root ->
                        Snackbar.make(root, getString(R.string.point_mainapp_demo_app_payment_success_ref, it.paymentReference), Snackbar.LENGTH_LONG).show()
                    }
                }.doIfError {
                    binding?.root?.let { root ->
                        Snackbar.make(root, it.message ?: getString(R.string.point_mainapp_demo_app_payment_error), Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
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
                        pointMainappDemoAppCartGoToPay.visibility = View.GONE
                    }
                } else {
                    binding?.apply {
                        pointMainappDemoAppCartEmpty.visibility = View.GONE
                        pointMainappDemoAppCartRecycler.visibility = View.VISIBLE
                        pointMainappDemoAppCartTotalContainer.visibility = View.VISIBLE
                        pointMainappDemoAppCartGoToPay.visibility = View.VISIBLE
                        pointMainappDemoAppCartTotalLabel.text =
                            getString(R.string.point_mainapp_demo_app_cart_total, String.format(Locale.US, "$%.2f", viewModel.totalAmount))
                    }
                    adapter.submitList(items)
                }
            }
        }
    }

    companion object {
        const val EXTRA_STORE_NAME = "extra_store_name"
    }
}
