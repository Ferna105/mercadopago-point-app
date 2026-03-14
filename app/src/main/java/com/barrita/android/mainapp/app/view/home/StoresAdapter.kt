package com.barrita.android.mainapp.app.view.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.util.ImageLoader
import com.barrita.android.mainapp.app.data.dto.Store
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppItemStoreBinding

class StoresAdapter(
    private val onStoreClick: (Store) -> Unit
) : ListAdapter<Store, StoresAdapter.StoreViewHolder>(StoreDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val binding = PointMainappDemoAppItemStoreBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StoreViewHolder(
        private val binding: PointMainappDemoAppItemStoreBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(store: Store) {
            binding.apply {
                pointMainappDemoAppStoreName.text = store.name
                pointMainappDemoAppStoreDescription.text = store.description ?: ""
                pointMainappDemoAppStoreSchedule.text = store.schedule ?: ""
                pointMainappDemoAppStoreStatus.text = if (store.status == "active") {
                    root.context.getString(R.string.point_mainapp_demo_app_store_status_active)
                } else {
                    root.context.getString(R.string.point_mainapp_demo_app_store_status_inactive)
                }
                loadStoreImage(store.logoUrl)
                root.setOnClickListener { onStoreClick(store) }
            }
        }

        private fun loadStoreImage(imageRef: String?) {
            val imageView = binding.pointMainappDemoAppStoreImage
            if (imageRef.isNullOrBlank()) {
                imageView.visibility = View.GONE
                return
            }
            imageView.visibility = View.VISIBLE
            if (imageRef.startsWith("http")) {
                ImageLoader.load(imageView, imageRef)
            } else {
                val resId = imageView.context.resources.getIdentifier(
                    imageRef, "drawable", imageView.context.packageName
                )
                if (resId != 0) imageView.setImageResource(resId)
                else imageView.visibility = View.GONE
            }
        }
    }

    private class StoreDiffCallback : DiffUtil.ItemCallback<Store>() {
        override fun areItemsTheSame(oldItem: Store, newItem: Store): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Store, newItem: Store): Boolean {
            return oldItem == newItem
        }
    }
}
