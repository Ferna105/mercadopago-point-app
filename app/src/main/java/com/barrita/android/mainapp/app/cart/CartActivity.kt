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
        setupHeader()
        setupRecyclerView()
        setupButtons()
        observeCart()
    }

    private fun setupHeader() {
        binding?.pointMainappDemoAppCartBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding?.pointMainappDemoAppCartHeaderStoreName?.text = storeName
        binding?.pointMainappDemoAppCartHeaderInitials?.text = getInitials(storeName)
    }

    private fun setupRecyclerView() {
        binding?.pointMainappDemoAppCartRecycler?.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            this.adapter = this@CartActivity.adapter
        }
    }

    private fun setupButtons() {
        binding?.pointMainappDemoAppCartKeepChoosing?.setOnClickListener {
            finish()
        }

        binding?.pointMainappDemoAppCartGoToPay?.setOnClickListener {
            launchPayment()
        }
    }

    private fun launchPayment() {
        val total = viewModel.totalAmount
        val description = getString(R.string.point_mainapp_demo_app_cart_payment_description, storeName)
        binding?.pointMainappDemoAppCartGoToPay?.isEnabled = false
        binding?.pointMainappDemoAppCartError?.visibility = View.GONE

        lifecycleScope.launch {
            val createdOrderId = createOrderBeforePayment(total)
            if (createdOrderId == null) {
                binding?.pointMainappDemoAppCartGoToPay?.isEnabled = true
                showError(getString(R.string.point_mainapp_demo_app_payment_error))
                return@launch
            }

            paymentFlow.launchPaymentFlow(
                PaymentFlowRequestData(
                    amount = total,
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
                    showError(error.message ?: getString(R.string.point_mainapp_demo_app_payment_error))
                }
            }
        }
    }

    private fun showError(message: String) {
        binding?.pointMainappDemoAppCartError?.apply {
            text = message
            visibility = View.VISIBLE
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

    private fun observeCart() {
        lifecycleScope.launch {
            viewModel.items.collect { items ->
                if (items.isEmpty()) {
                    binding?.apply {
                        pointMainappDemoAppCartEmpty.visibility = View.VISIBLE
                        pointMainappDemoAppCartScroll.visibility = View.GONE
                        pointMainappDemoAppCartBottom.visibility = View.GONE
                    }
                } else {
                    val totalItems = items.sumOf { it.quantity }
                    val totalAmount = viewModel.totalAmount
                    val totalFormatted = formatPrice(totalAmount)

                    binding?.apply {
                        pointMainappDemoAppCartEmpty.visibility = View.GONE
                        pointMainappDemoAppCartScroll.visibility = View.VISIBLE
                        pointMainappDemoAppCartBottom.visibility = View.VISIBLE

                        val countText = if (totalItems == 1)
                            getString(R.string.point_mainapp_demo_app_cart_product_count_one)
                        else
                            getString(R.string.point_mainapp_demo_app_cart_product_count, totalItems)
                        pointMainappDemoAppCartSubtotalLabel.text =
                            "${getString(R.string.point_mainapp_demo_app_cart_subtotal)} $countText"
                        pointMainappDemoAppCartSubtotalAmount.text = totalFormatted
                        pointMainappDemoAppCartTotalAmount.text = totalFormatted
                        pointMainappDemoAppCartGoToPay.text =
                            getString(R.string.point_mainapp_demo_app_cart_pay_amount, totalFormatted)
                    }
                    adapter.submitList(items)
                }
            }
        }
    }

    private fun formatPrice(price: Double): String =
        String.format(Locale.US, "$%,.0f", price)

    private fun getInitials(name: String): String =
        name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")

    companion object {
        const val EXTRA_STORE_NAME = "extra_store_name"
        const val EXTRA_STORE_ID = "extra_store_id"
    }
}
