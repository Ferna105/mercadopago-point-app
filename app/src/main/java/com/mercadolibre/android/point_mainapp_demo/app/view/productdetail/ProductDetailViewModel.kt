package com.mercadolibre.android.point_mainapp_demo.app.view.productdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.Product
import com.mercadolibre.android.point_mainapp_demo.app.model.ProductDetailManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class ProductDetailState {
    object Loading : ProductDetailState()
    data class Success(val product: Product) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}

class ProductDetailViewModel : ViewModel() {

    private val _state = MutableLiveData<ProductDetailState>()
    val state: LiveData<ProductDetailState> get() = _state

    private val manager = ProductDetailManager()

    fun loadDetail(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.postValue(ProductDetailState.Loading)
            manager.fetchProductDetail(productId)
                .catch { e ->
                    _state.postValue(ProductDetailState.Error(e.message ?: "Unknown error"))
                }
                .collect { product ->
                    _state.postValue(ProductDetailState.Success(product))
                }
        }
    }
}
