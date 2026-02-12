package com.mercadolibre.android.point_mainapp_demo.app.view.storeproductslist.adapter

import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Product
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Store

sealed class StoreListItem {
    data class StoreHeader(val store: Store) : StoreListItem()
    data class CategoryHeader(val category: String) : StoreListItem()
    data class ProductItem(val product: Product) : StoreListItem()
}
