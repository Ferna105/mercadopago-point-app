package com.mercadolibre.android.point_mainapp_demo.app.model

import com.mercadolibre.android.point_mainapp_demo.app.data.NetworkDependencyProvider
import com.mercadolibre.android.point_mainapp_demo.app.data.ItemsService
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.PokemonListResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class ItemsListManager(
    private val itemsService: ItemsService = NetworkDependencyProvider.itemsService
) {
    suspend fun fetchItems(): Flow<Response<PokemonListResponse>> {
        return flow {
            emit(itemsService.getPokemon())
        }
    }
}
