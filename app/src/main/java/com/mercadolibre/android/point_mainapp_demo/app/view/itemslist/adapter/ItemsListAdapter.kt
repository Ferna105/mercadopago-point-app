package com.mercadolibre.android.point_mainapp_demo.app.view.itemslist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mercadolibre.android.point_mainapp_demo.app.data.dto.PokemonResult
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppItemElementBinding

class ItemsListAdapter(
    private val onItemClick: (PokemonResult) -> Unit = {}
) : ListAdapter<PokemonResult, ItemsListAdapter.ItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = PointMainappDemoAppItemElementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(
        private val binding: PointMainappDemoAppItemElementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PokemonResult) {
            binding.pointMainappDemoAppItemName.text = item.name
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PokemonResult>() {
        override fun areItemsTheSame(oldItem: PokemonResult, newItem: PokemonResult): Boolean =
            oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: PokemonResult, newItem: PokemonResult): Boolean =
            oldItem == newItem
    }
}
