package com.mercadolibre.android.point_mainapp_demo.app.view.pokemondetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.PokemonDetailResponse
import com.mercadolibre.android.point_mainapp_demo.app.model.PokemonDetailManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class PokemonDetailState {
    object Loading : PokemonDetailState()
    data class Success(val pokemon: PokemonDetailResponse) : PokemonDetailState()
    data class Error(val message: String) : PokemonDetailState()
}

class PokemonDetailViewModel : ViewModel() {

    private val _state = MutableLiveData<PokemonDetailState>()
    val state: LiveData<PokemonDetailState> get() = _state

    private val manager = PokemonDetailManager()

    fun loadDetail(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.postValue(PokemonDetailState.Loading)
            manager.fetchDetail(url)
                .catch { e ->
                    _state.postValue(PokemonDetailState.Error(e.message ?: "Unknown error"))
                }
                .collect { response ->
                    if (response.isSuccessful) {
                        response.body()?.let { detail ->
                            _state.postValue(PokemonDetailState.Success(detail))
                        } ?: _state.postValue(PokemonDetailState.Error("Empty response"))
                    } else {
                        _state.postValue(
                            PokemonDetailState.Error(
                                response.errorBody()?.string() ?: "Error ${response.code()}"
                            )
                        )
                    }
                }
        }
    }
}
