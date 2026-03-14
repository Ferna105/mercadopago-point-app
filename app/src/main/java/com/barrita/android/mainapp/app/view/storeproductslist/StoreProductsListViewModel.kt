package com.barrita.android.mainapp.app.view.storeproductslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barrita.android.mainapp.app.data.NetworkDependencyProvider
import com.barrita.android.mainapp.app.data.SessionManager
import com.barrita.android.mainapp.app.data.dto.Product
import com.barrita.android.mainapp.app.data.dto.RefreshRequest
import com.barrita.android.mainapp.app.data.dto.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class StoreProductsListState {
    object Loading : StoreProductsListState()
    data class Success(val store: Store, val products: List<Product>) : StoreProductsListState()
    data class Error(val message: String) : StoreProductsListState()
    object TokenExpired : StoreProductsListState()
}

class StoreProductsListViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableLiveData<StoreProductsListState>()
    val state: LiveData<StoreProductsListState> get() = _state

    private val productsService = NetworkDependencyProvider.productsService
    private val authService = NetworkDependencyProvider.authService

    private var currentStore: Store? = null
    private var currentStoreId: String = ""

    fun loadProducts(storeId: String, store: Store?) {
        currentStoreId = storeId
        currentStore = store
        _state.postValue(StoreProductsListState.Loading)

        val accessToken = SessionManager.getAccessToken(getApplication())
        if (accessToken == null) {
            _state.postValue(StoreProductsListState.TokenExpired)
            return
        }

        fetchProducts(accessToken, isRetry = false)
    }

    private fun fetchProducts(accessToken: String, isRetry: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // #region agent log
                Log.d("DEBUG_0b902f", "[PRODUCTS] Fetching products for storeId=$currentStoreId, isRetry=$isRetry")
                // #endregion
                val response = productsService.getProducts("Bearer $accessToken", currentStoreId)
                // #region agent log
                Log.d("DEBUG_0b902f", "[PRODUCTS] Response code=${response.code()}, isSuccessful=${response.isSuccessful}")
                // #endregion

                when {
                    response.isSuccessful -> {
                        val products = response.body()?.data ?: emptyList()
                        // #region agent log
                        Log.d("DEBUG_0b902f", "[PRODUCTS] SUCCESS: Loaded ${products.size} products")
                        // #endregion
                        val store = currentStore ?: createDefaultStore()
                        _state.postValue(StoreProductsListState.Success(store, products))
                    }
                    response.code() == 401 && !isRetry -> {
                        // #region agent log
                        Log.d("DEBUG_0b902f", "[PRODUCTS] Got 401, attempting token refresh...")
                        // #endregion
                        tryRefreshToken()
                    }
                    else -> {
                        // #region agent log
                        val errorBody = response.errorBody()?.string()
                        Log.d("DEBUG_0b902f", "[PRODUCTS] HTTP error ${response.code()}, errorBody=$errorBody")
                        // #endregion
                        _state.postValue(StoreProductsListState.Error("Error al cargar productos"))
                    }
                }
            } catch (e: Exception) {
                // #region agent log
                Log.d("DEBUG_0b902f", "[PRODUCTS] EXCEPTION: ${e.javaClass.simpleName}: ${e.message}")
                // #endregion
                _state.postValue(StoreProductsListState.Error(e.message ?: "Error desconocido"))
            }
        }
    }

    private suspend fun tryRefreshToken() {
        val refreshToken = SessionManager.getRefreshToken(getApplication())
        if (refreshToken == null) {
            // #region agent log
            Log.d("DEBUG_0b902f", "[PRODUCTS] No refresh token, emitting TokenExpired")
            // #endregion
            _state.postValue(StoreProductsListState.TokenExpired)
            return
        }

        try {
            // #region agent log
            Log.d("DEBUG_0b902f", "[PRODUCTS] Calling refresh endpoint...")
            // #endregion
            val response = authService.refreshToken(RefreshRequest(refreshToken))

            if (response.isSuccessful) {
                val body = response.body()
                val newAccessToken = body?.accessToken
                val newRefreshToken = body?.refreshToken

                if (newAccessToken != null && newRefreshToken != null) {
                    // #region agent log
                    Log.d("DEBUG_0b902f", "[PRODUCTS] Refresh SUCCESS, retrying products request")
                    // #endregion
                    SessionManager.updateTokens(getApplication(), newAccessToken, newRefreshToken)
                    fetchProducts(newAccessToken, isRetry = true)
                } else {
                    _state.postValue(StoreProductsListState.TokenExpired)
                }
            } else {
                // #region agent log
                Log.d("DEBUG_0b902f", "[PRODUCTS] Refresh FAILED: HTTP ${response.code()}")
                // #endregion
                _state.postValue(StoreProductsListState.TokenExpired)
            }
        } catch (e: Exception) {
            // #region agent log
            Log.d("DEBUG_0b902f", "[PRODUCTS] Refresh EXCEPTION: ${e.message}")
            // #endregion
            _state.postValue(StoreProductsListState.TokenExpired)
        }
    }

    private fun createDefaultStore(): Store {
        return Store(
            id = currentStoreId,
            name = "Tienda",
            description = null,
            logoUrl = null,
            bannerUrl = null,
            slug = null,
            phone = null,
            address = null,
            schedule = null,
            paymentMethodId = null,
            status = "active"
        )
    }
}
