package com.mercadolibre.android.point_mainapp_demo.app.model

import com.mercadolibre.android.point_mainapp_demo.app.data.NetworkDependencyProvider
import com.mercadolibre.android.point_mainapp_demo.app.data.ItemsService
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.PokemonDetailResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class PokemonDetailManager(
    private val itemsService: ItemsService = NetworkDependencyProvider.itemsService
) {
    suspend fun fetchDetail(url: String): Flow<Response<PokemonDetailResponse>> {
        return flow {
            emit(itemsService.getPokemonDetail(url))
        }
    }
}
