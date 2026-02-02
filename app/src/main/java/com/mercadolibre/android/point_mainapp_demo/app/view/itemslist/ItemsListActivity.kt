package com.mercadolibre.android.point_mainapp_demo.app.view.itemslist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppActivityItemsListBinding
import com.mercadolibre.android.point_mainapp_demo.app.view.itemslist.adapter.ItemsListAdapter
import com.mercadolibre.android.point_mainapp_demo.app.view.pokemondetail.PokemonDetailActivity

class ItemsListActivity : AppCompatActivity() {

    private var binding: PointMainappDemoAppActivityItemsListBinding? = null
    private val viewModel: ItemsListViewModel by viewModels()
    private val adapter = ItemsListAdapter { pokemon ->
        startActivity(
            Intent(this, PokemonDetailActivity::class.java).putExtra(
                PokemonDetailActivity.EXTRA_POKEMON_URL,
                pokemon.url
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PointMainappDemoAppActivityItemsListBinding.inflate(layoutInflater)
        binding?.run { setContentView(root) }
        setupRecyclerView()
        setupObservers()
        viewModel.loadItems()
    }

    private fun setupRecyclerView() {
        binding?.pointMainappDemoAppItemsRecycler?.apply {
            layoutManager = LinearLayoutManager(this@ItemsListActivity)
            adapter = this@ItemsListActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is ItemsListState.Loading -> showLoading()
                is ItemsListState.Success -> showList(state.items)
                is ItemsListState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        binding?.apply {
            pointMainappDemoAppItemsProgress.visibility = View.VISIBLE
            pointMainappDemoAppItemsRecycler.visibility = View.GONE
            pointMainappDemoAppItemsError.visibility = View.GONE
        }
    }

    private fun showList(items: List<com.mercadolibre.android.point_mainapp_demo.app.data.dto.PokemonResult>) {
        binding?.apply {
            pointMainappDemoAppItemsProgress.visibility = View.GONE
            pointMainappDemoAppItemsError.visibility = View.GONE
            pointMainappDemoAppItemsRecycler.visibility = View.VISIBLE
        }
        adapter.submitList(items)
    }

    private fun showError(message: String) {
        binding?.apply {
            pointMainappDemoAppItemsProgress.visibility = View.GONE
            pointMainappDemoAppItemsRecycler.visibility = View.GONE
            pointMainappDemoAppItemsError.visibility = View.VISIBLE
            pointMainappDemoAppItemsError.text = message
        }
    }
}
