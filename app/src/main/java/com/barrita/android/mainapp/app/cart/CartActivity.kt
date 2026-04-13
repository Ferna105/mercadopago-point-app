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
import com.barrita.android.mainapp.app.data.dto.CreateOrderData
import com.barrita.android.mainapp.app.data.dto.CreateOrderItemRequest
import com.barrita.android.mainapp.app.data.dto.CreateOrderRequest
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityCartBinding
import com.barrita.android.mainapp.app.util.PaymentConfirmationDateFormatter
import com.barrita.android.mainapp.app.util.ReceiptBitmapComposer
import com.barrita.android.mainapp.app.util.toast
import com.google.android.material.snackbar.Snackbar
import com.mercadolibre.android.point_integration_sdk.nativesdk.MPManager
import com.mercadolibre.android.point_integration_sdk.nativesdk.message.utils.doIfError
import com.mercadolibre.android.point_integration_sdk.nativesdk.message.utils.doIfSuccess
import com.mercadolibre.android.point_integration_sdk.nativesdk.payment.data.PaymentFlowRequestData
import com.mercadolibre.android.point_integration_sdk.nativesdk.payment.data.PaymentResponse
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
            val createdOrder = createOrderBeforePayment(total)
            if (createdOrder?.orderId == null) {
                binding?.pointMainappDemoAppCartGoToPay?.isEnabled = true
                showError(getString(R.string.point_mainapp_demo_app_payment_error))
                return@launch
            }
            val orderId = createdOrder.orderId
            val orderCode = createdOrder.orderCode ?: orderId

            paymentFlow.launchPaymentFlow(
                PaymentFlowRequestData(
                    amount = total,
                    description = description,
                    paymentMethod = null,
                    printOnTerminal = false,
                    taxes = null
                )
            ) { response ->
                binding?.pointMainappDemoAppCartGoToPay?.isEnabled = true
                response.doIfSuccess { paymentResponse ->
                    val cartSnapshot = CartRepository.items.value.toList()
                    lifecycleScope.launch {
                        confirmPaymentResult(
                            orderId = orderId,
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
                    printPurchaseReceipt(
                        cartSnapshot = cartSnapshot,
                        paymentResponse = paymentResponse,
                        orderCode = orderCode,
                        fallbackTotal = total
                    )
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
                            orderId = orderId,
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

    private suspend fun createOrderBeforePayment(total: Double): CreateOrderData? {
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
            if (response.isSuccessful) response.body()?.data else null
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

    private fun printPurchaseReceipt(
        cartSnapshot: List<CartItem>,
        paymentResponse: PaymentResponse,
        orderCode: String,
        fallbackTotal: Double,
    ) {
        try {
            val body = buildReceiptPlainText(
                cartSnapshot = cartSnapshot,
                paymentResponse = paymentResponse,
                orderCode = orderCode,
                fallbackTotal = fallbackTotal
            )
            val bitmap = ReceiptBitmapComposer.compose(body, orderCode)
            MPManager.bitmapPrinter.print(bitmap) { printResponse ->
                printResponse.doIfError { error ->
                    runOnUiThread {
                        val suffix = error.message?.let { ": $it" }.orEmpty()
                        toast(getString(R.string.point_mainapp_demo_app_cart_receipt_print_error) + suffix)
                    }
                }
            }
        } catch (_: Exception) {
            runOnUiThread {
                toast(getString(R.string.point_mainapp_demo_app_cart_receipt_print_error))
            }
        }
    }

    private fun buildReceiptPlainText(
        cartSnapshot: List<CartItem>,
        paymentResponse: PaymentResponse,
        orderCode: String,
        fallbackTotal: Double,
    ): String = buildString {
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_title))
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_separator))
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_store, storeName))
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_order_id, orderCode))
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_separator))
        cartSnapshot.forEach { item ->
            appendLine(
                getString(
                    R.string.point_mainapp_demo_app_cart_receipt_item,
                    item.quantity,
                    item.product.name,
                    formatPrice(item.subtotal)
                )
            )
        }
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_separator))
        val paid = runCatching { paymentResponse.paymentAmount.toDouble() }.getOrElse { fallbackTotal }
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_total, formatPrice(paid)))
        paymentResponse.paymentReference.takeIf { it.isNotBlank() }?.let { ref ->
            appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_ref, ref))
        }
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_method, paymentResponse.paymentMethod.toString()))
        val brand = paymentResponse.paymentBrandName.takeIf { it.isNotBlank() }
        val lastFour = paymentResponse.paymentLastFourDigits.takeIf { it.isNotBlank() }.orEmpty()
        if (brand != null) {
            appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_card, brand, lastFour))
        }
        paymentResponse.paymentInstallments.takeIf { it.isNotBlank() }?.let { inst ->
            appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_installments, inst))
        }
        paymentResponse.paymentCreationDate.takeIf { it.isNotBlank() }?.let { date ->
            appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_date, date))
        }
        paymentResponse.tipAmount.takeIf { it.isNotBlank() && it != "0" && it != "0.0" }?.let { tip ->
            appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_tip, tip))
        }
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_separator))
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_thanks))
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_separator))
        appendLine(getString(R.string.point_mainapp_demo_app_cart_receipt_qr_caption))
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
