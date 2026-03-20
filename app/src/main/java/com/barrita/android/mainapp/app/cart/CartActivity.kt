package com.barrita.android.mainapp.app.cart

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.data.NetworkDependencyProvider
import com.barrita.android.mainapp.app.data.dto.ConfirmPaymentRequest
import com.barrita.android.mainapp.app.data.dto.CreateOrderItemRequest
import com.barrita.android.mainapp.app.data.dto.CreateOrderRequest
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityCartBinding
import com.barrita.android.mainapp.app.util.PaymentConfirmationDateFormatter
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
    private val storeId: String?
        get() = intent.getStringExtra(EXTRA_STORE_ID)

    private val paymentFlow = MPManager.paymentFlow
    private val ordersService = NetworkDependencyProvider.ordersService

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
            lifecycleScope.launch {
                val createdOrderId = createOrderBeforePayment(total)
                if (createdOrderId == null) {
                    binding?.pointMainappDemoAppCartGoToPay?.isEnabled = true
                    showErrorSnackBar(getString(R.string.point_mainapp_demo_app_payment_error))
                    return@launch
                }

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
                    response.doIfSuccess { paymentResponse ->
                        lifecycleScope.launch {
                            confirmPaymentResult(
                                orderId = createdOrderId,
                                status = "approved",
                                paymentReference = paymentResponse.paymentReference,
                                amount = paymentResponse.paymentAmount.toString().toDoubleOrNull() ?: total,
                                method = paymentResponse.paymentMethod.toString(),
                                brand = paymentResponse.paymentBrandName,
                                lastFour = paymentResponse.paymentLastFourDigits,
                                createdAt = PaymentConfirmationDateFormatter.toIso8601OrNull(
                                    paymentResponse.paymentCreationDate
                                ),
                                tip = paymentResponse.tipAmount.toString().toDoubleOrNull()
                            )
                        }
                        viewModel.clearCart()
                        binding?.root?.let { root ->
                            Snackbar.make(
                                root,
                                getString(
                                    R.string.point_mainapp_demo_app_payment_success_ref,
                                    paymentResponse.paymentReference
                                ),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }.doIfError { error ->
                        lifecycleScope.launch {
                            confirmPaymentResult(
                                orderId = createdOrderId,
                                status = "canceled",
                                errorMessage = error.message
                            )
                        }
                        showErrorSnackBar(error.message ?: getString(R.string.point_mainapp_demo_app_payment_error))
                    }
                }
            }
        }
    }

    private suspend fun createOrderBeforePayment(total: Double): String? {
        val currentStoreId = storeId ?: return null
        val cartItems = CartRepository.items.value
        if (cartItems.isEmpty()) return null

        return try {
            val response = ordersService.createOrder(
                CreateOrderRequest(
                    storeId = currentStoreId,
                    qrCode = null,
                    items = cartItems.map {
                        CreateOrderItemRequest(
                            productId = it.product.id,
                            quantity = it.quantity,
                            price = it.product.price
                        )
                    },
                    total = total
                )
            )
            if (response.isSuccessful) response.body()?.data?.orderId else null
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun confirmPaymentResult(
        orderId: String,
        status: String,
        paymentReference: String? = null,
        amount: Double? = null,
        method: String? = null,
        brand: String? = null,
        lastFour: String? = null,
        createdAt: String? = null,
        tip: Double? = null,
        errorMessage: String? = null,
    ) {
        try {
            ordersService.confirmPayment(
                ConfirmPaymentRequest(
                    orderId = orderId,
                    paymentReference = paymentReference,
                    status = status,
                    amount = amount,
                    method = method,
                    brand = brand,
                    lastFour = lastFour,
                    createdAt = createdAt,
                    tip = tip,
                    errorMessage = errorMessage,
                    sourceChannel = "point_app"
                )
            )
        } catch (_: Exception) {
        }
    }

    private fun showErrorSnackBar(message: String) {
        binding?.root?.let { root ->
            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
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
        const val EXTRA_STORE_ID = "extra_store_id"
    }
}
