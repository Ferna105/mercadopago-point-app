package com.mercadolibre.android.point_mainapp_demo.app.view.itemslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.PokemonResult
import com.mercadolibre.android.point_mainapp_demo.app.model.ItemsListManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class ItemsListState {
    object Loading : ItemsListState()
    data class Success(val items: List<PokemonResult>) : ItemsListState()
    data class Error(val message: String) : ItemsListState()
}

class ItemsListViewModel : ViewModel() {

    private val _state = MutableLiveData<ItemsListState>()
    val state: LiveData<ItemsListState> get() = _state

    private val manager = ItemsListManager()

    fun loadItems() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.postValue(ItemsListState.Loading)
            manager.fetchItems()
                .catch { e ->
                    _state.postValue(ItemsListState.Error(e.message ?: "Unknown error"))
                }
                .collect { response ->
                    if (response.isSuccessful) {
                        val list = response.body()?.results ?: emptyList()
                        _state.postValue(ItemsListState.Success(list))
                    } else {
                        _state.postValue(
                            ItemsListState.Error(
                                response.errorBody()?.string() ?: "Error ${response.code()}"
                            )
                        )
                    }
                }
        }
    }
}
