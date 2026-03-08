package com.barrita.android.mainapp.app.view.storeproductslist.adapter

import com.barrita.android.mainapp.app.data.dto.Product
import com.barrita.android.mainapp.app.data.dto.Store

sealed class StoreListItem {
    data class StoreHeader(val store: Store) : StoreListItem()
    data class CategoryHeader(val category: String) : StoreListItem()
    data class ProductItem(val product: Product) : StoreListItem()
}
