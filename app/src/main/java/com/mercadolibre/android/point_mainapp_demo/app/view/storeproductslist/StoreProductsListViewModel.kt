package com.mercadolibre.android.point_mainapp_demo.app.view.storeproductslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Product
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Store
import com.mercadolibre.android.point_mainapp_demo.app.model.StoreProductsListManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class StoreProductsListState {
    object Loading : StoreProductsListState()
    data class Success(val store: Store, val products: List<Product>) : StoreProductsListState()
    data class Error(val message: String) : StoreProductsListState()
}

class StoreProductsListViewModel : ViewModel() {

    private val _state = MutableLiveData<StoreProductsListState>()
    val state: LiveData<StoreProductsListState> get() = _state

    private val manager = StoreProductsListManager()

    fun loadProducts(storeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.postValue(StoreProductsListState.Loading)
            manager.fetchStoreProducts(storeId)
                .catch { e ->
                    _state.postValue(StoreProductsListState.Error(e.message ?: "Unknown error"))
                }
                .collect { response ->
                    _state.postValue(StoreProductsListState.Success(response.store, response.products))
                }
        }
    }
}
