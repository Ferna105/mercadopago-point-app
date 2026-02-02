package com.mercadolibre.android.point_mainapp_demo.app.data

import com.mercadolibre.android.point_mainapp_demo.app.data.dto.PokemonDetailResponse
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.PokemonListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ItemsService {
    @GET(NetworkConstants.POKEMON_PATH)
    suspend fun getPokemon(): Response<PokemonListResponse>

    @GET
    suspend fun getPokemonDetail(@Url url: String): Response<PokemonDetailResponse>
}
