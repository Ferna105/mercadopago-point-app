package com.mercadolibre.android.point_mainapp_demo.app.data.dto

import com.google.gson.annotations.SerializedName

data class PokemonDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("base_experience") val baseExperience: Int?,
    @SerializedName("height") val height: Int,
    @SerializedName("weight") val weight: Int,
    @SerializedName("sprites") val sprites: PokemonSprites?,
    @SerializedName("types") val types: List<PokemonTypeSlot>?
)

data class PokemonSprites(
    @SerializedName("front_default") val frontDefault: String?
)

data class PokemonTypeSlot(
    @SerializedName("slot") val slot: Int,
    @SerializedName("type") val type: PokemonTypeInfo
)

data class PokemonTypeInfo(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)
