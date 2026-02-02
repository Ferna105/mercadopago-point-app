package com.mercadolibre.android.point_mainapp_demo.app.view.pokemondetail

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mercadolibre.android.point_mainapp_demo.app.R
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.PokemonDetailResponse
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppActivityPokemonDetailBinding

class PokemonDetailActivity : AppCompatActivity() {

    private var binding: PointMainappDemoAppActivityPokemonDetailBinding? = null
    private val viewModel: PokemonDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PointMainappDemoAppActivityPokemonDetailBinding.inflate(layoutInflater)
        binding?.run { setContentView(root) }
        val url = intent.getStringExtra(EXTRA_POKEMON_URL) ?: run {
            finish()
            return
        }
        setupObservers()
        viewModel.loadDetail(url)
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is PokemonDetailState.Loading -> showLoading()
                is PokemonDetailState.Success -> showDetail(state.pokemon)
                is PokemonDetailState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        binding?.apply {
            pointMainappDemoAppDetailProgress.visibility = View.VISIBLE
            pointMainappDemoAppDetailContent.visibility = View.GONE
            pointMainappDemoAppDetailError.visibility = View.GONE
        }
    }

    private fun showDetail(pokemon: PokemonDetailResponse) {
        binding?.apply {
            pointMainappDemoAppDetailProgress.visibility = View.GONE
            pointMainappDemoAppDetailError.visibility = View.GONE
            pointMainappDemoAppDetailContent.visibility = View.VISIBLE

            pointMainappDemoAppDetailName.text = pokemon.name.replaceFirstChar { it.uppercase() }
            pointMainappDemoAppDetailId.text = "#${pokemon.id}"
            pointMainappDemoAppDetailHeightWeight.text = getString(
                R.string.point_mainapp_demo_app_pokemon_height_weight,
                pokemon.height,
                pokemon.weight
            )
            pointMainappDemoAppDetailTypes.text = getString(
                R.string.point_mainapp_demo_app_pokemon_types,
                pokemon.types?.joinToString(", ") { it.type.name } ?: "-"
            )
            pointMainappDemoAppDetailExperience.text = getString(
                R.string.point_mainapp_demo_app_pokemon_experience,
                pokemon.baseExperience ?: 0
            )
        }
    }

    private fun showError(message: String) {
        binding?.apply {
            pointMainappDemoAppDetailProgress.visibility = View.GONE
            pointMainappDemoAppDetailContent.visibility = View.GONE
            pointMainappDemoAppDetailError.visibility = View.VISIBLE
            pointMainappDemoAppDetailError.text = message
        }
    }

    companion object {
        const val EXTRA_POKEMON_URL = "extra_pokemon_url"
    }
}
