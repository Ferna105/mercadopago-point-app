package com.barrita.android.mainapp.app.data

object NetworkConstants {
    const val MP_BASE_URL = "https://api.mercadopago.com/"
    const val REFUNDS_PATH = "v1/payments/{payment_id}/refunds"

    const val BARRITA_BASE_URL = "https://api.barrita.app/"
    const val LOGIN_PATH = "auth/login"
    const val REFRESH_PATH = "auth/refresh"
    const val STORES_PATH = "stores"
    const val PRODUCTS_PATH = "admin/products"

    const val READ_TIMEOUT = 15L
    const val CONNECTION_TIMEOUT = 15L
}
