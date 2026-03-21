package com.barrita.android.mainapp.app.view.storeproductslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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

data class ProductsUiState(
    val store: Store? = null,
    val allProducts: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val allCategoryNames: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val tokenExpired: Boolean = false
)

class StoreProductsListViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableLiveData<StoreProductsListState>()
    val state: LiveData<StoreProductsListState> get() = _state

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _selectedCategory = MutableLiveData<String?>(null)
    val selectedCategory: LiveData<String?> get() = _selectedCategory

    private val _allCategoryNames = MutableLiveData<List<String>>(emptyList())
    val allCategoryNames: LiveData<List<String>> get() = _allCategoryNames

    private var allProducts: List<Product> = emptyList()

    private val _filteredProducts = MediatorLiveData<List<Product>>().apply {
        addSource(_state) { refilter() }
        addSource(_searchQuery) { refilter() }
        addSource(_selectedCategory) { refilter() }
    }
    val filteredProducts: LiveData<List<Product>> get() = _filteredProducts

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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    private fun refilter() {
        val query = _searchQuery.value.orEmpty().trim().lowercase()
        val category = _selectedCategory.value

        var result = allProducts

        if (query.isNotEmpty()) {
            result = result.filter { p ->
                p.name.lowercase().contains(query) ||
                    (p.description?.lowercase()?.contains(query) == true)
            }
        }

        if (category != null) {
            result = result.filter { p ->
                p.resolvedCategories.contains(category)
            }
        }

        _filteredProducts.value = result
    }

    private fun extractCategories(products: List<Product>): List<String> {
        val names = mutableSetOf<String>()
        products.forEach { p ->
            p.resolvedCategories.forEach { names.add(it) }
        }
        return names.sorted()
    }

    private fun fetchProducts(accessToken: String, isRetry: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = productsService.getProducts("Bearer $accessToken", currentStoreId)

                when {
                    response.isSuccessful -> {
                        val products = response.body()?.data ?: emptyList()
                        allProducts = products
                        val store = currentStore ?: createDefaultStore()
                        _allCategoryNames.postValue(extractCategories(products))
                        _state.postValue(StoreProductsListState.Success(store, products))
                    }
                    response.code() == 401 && !isRetry -> {
                        tryRefreshToken()
                    }
                    else -> {
                        _state.postValue(StoreProductsListState.Error("Error al cargar productos"))
                    }
                }
            } catch (e: Exception) {
                _state.postValue(StoreProductsListState.Error(e.message ?: "Error desconocido"))
            }
        }
    }

    private suspend fun tryRefreshToken() {
        val refreshToken = SessionManager.getRefreshToken(getApplication())
        if (refreshToken == null) {
            _state.postValue(StoreProductsListState.TokenExpired)
            return
        }

        try {
            val response = authService.refreshToken(RefreshRequest(refreshToken))

            if (response.isSuccessful) {
                val body = response.body()
                val newAccessToken = body?.accessToken
                val newRefreshToken = body?.refreshToken

                if (newAccessToken != null && newRefreshToken != null) {
                    SessionManager.updateTokens(getApplication(), newAccessToken, newRefreshToken)
                    fetchProducts(newAccessToken, isRetry = true)
                } else {
                    _state.postValue(StoreProductsListState.TokenExpired)
                }
            } else {
                _state.postValue(StoreProductsListState.TokenExpired)
            }
        } catch (e: Exception) {
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
