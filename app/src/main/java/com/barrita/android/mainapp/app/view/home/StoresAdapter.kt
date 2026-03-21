package com.barrita.android.mainapp.app.view.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.data.dto.Store
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppItemStoreBinding
import com.barrita.android.mainapp.app.util.ImageLoader

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
        holder.bind(getItem(position), position)
    }

    inner class StoreViewHolder(
        private val binding: PointMainappDemoAppItemStoreBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(store: Store, position: Int) {
            val ctx = binding.root.context
            val isActive = store.status == "active"
            val palette = if (isActive) BANNER_PALETTES[position % BANNER_PALETTES.size] else DISABLED_PALETTE

            val bannerGradient = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.parseColor(palette.from), Color.parseColor(palette.to))
            )
            bannerGradient.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
            binding.pointMainappDemoAppStoreBanner.background = bannerGradient

            val badgeBg = GradientDrawable()
            badgeBg.setColor(Color.parseColor(palette.badgeBg))
            badgeBg.cornerRadius = dpToPx(12f)
            binding.pointMainappDemoAppStoreBadgeContainer.background = badgeBg

            binding.pointMainappDemoAppStoreStatus.text = if (isActive)
                ctx.getString(R.string.point_mainapp_demo_app_stores_open_now)
            else
                ctx.getString(R.string.point_mainapp_demo_app_stores_disabled)
            binding.pointMainappDemoAppStoreStatus.setTextColor(Color.parseColor(palette.badgeText))

            val dotBg = GradientDrawable()
            dotBg.shape = GradientDrawable.OVAL
            dotBg.setColor(Color.parseColor(palette.badgeDot))
            binding.pointMainappDemoAppStoreBadgeDot.background = dotBg

            loadStoreAvatar(store, palette)

            binding.pointMainappDemoAppStoreName.text = store.name
            binding.pointMainappDemoAppStoreName.setTextColor(
                if (isActive) ctx.getColor(R.color.barrita_text_primary)
                else ctx.getColor(R.color.barrita_text_muted)
            )

            val desc = store.description
            if (!desc.isNullOrBlank()) {
                binding.pointMainappDemoAppStoreDescription.text = desc
            } else {
                binding.pointMainappDemoAppStoreDescription.text = ""
            }

            val schedule = store.schedule
            binding.pointMainappDemoAppStoreSchedule.text =
                if (!schedule.isNullOrBlank()) schedule
                else ctx.getString(R.string.point_mainapp_demo_app_stores_no_schedule)

            binding.pointMainappDemoAppStorePayment.text =
                if (store.paymentMethodId != null) ctx.getString(R.string.point_mainapp_demo_app_stores_mercado_pago)
                else ctx.getString(R.string.point_mainapp_demo_app_stores_no_payment)

            binding.root.setOnClickListener { onStoreClick(store) }
        }

        private fun loadStoreAvatar(store: Store, palette: BannerPalette) {
            val imageRef = store.logoUrl
            val imageView = binding.pointMainappDemoAppStoreAvatarImage
            val initialsView = binding.pointMainappDemoAppStoreAvatarInitials

            val avatarBg = GradientDrawable()
            avatarBg.setColor(Color.parseColor(palette.avatar))
            avatarBg.cornerRadius = dpToPx(12f)
            initialsView.background = avatarBg
            initialsView.text = getInitials(store.name)

            if (!imageRef.isNullOrBlank()) {
                initialsView.visibility = View.VISIBLE

                val imgBg = GradientDrawable()
                imgBg.setColor(Color.WHITE)
                imgBg.cornerRadius = dpToPx(12f)
                imageView.background = imgBg
                imageView.setImageDrawable(null)

                if (imageRef.startsWith("http")) {
                    imageView.visibility = View.GONE
                    ImageLoader.load(imageView, imageRef)
                } else {
                    val resId = imageView.context.resources.getIdentifier(
                        imageRef, "drawable", imageView.context.packageName
                    )
                    if (resId != 0) {
                        imageView.visibility = View.VISIBLE
                        imageView.setImageResource(resId)
                    } else {
                        imageView.visibility = View.GONE
                    }
                }
            } else {
                initialsView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
                imageView.setImageDrawable(null)
            }
        }

        private fun dpToPx(dp: Float): Float =
            dp * binding.root.context.resources.displayMetrics.density

        private fun getInitials(name: String): String =
            name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
    }

    private class StoreDiffCallback : DiffUtil.ItemCallback<Store>() {
        override fun areItemsTheSame(oldItem: Store, newItem: Store): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Store, newItem: Store): Boolean =
            oldItem == newItem
    }

    companion object {
        data class BannerPalette(
            val from: String,
            val to: String,
            val avatar: String,
            val badgeBg: String,
            val badgeText: String,
            val badgeDot: String
        )

        val BANNER_PALETTES = listOf(
            BannerPalette("#9AD4D2", "#C6EAE9", "#0c6c6b", "#D8F4F3EB", "#0c6c6b", "#0c6c6b"),
            BannerPalette("#C4B5F4", "#E4DAF8", "#6b40c0", "#E4DAF8EB", "#6b40c0", "#6b40c0"),
            BannerPalette("#F4D2B5", "#F8E8DA", "#c07830", "#F8E8DAEB", "#c07830", "#c07830"),
            BannerPalette("#B5D4F4", "#DAE8F8", "#3068c0", "#DAE8F8EB", "#3068c0", "#3068c0"),
            BannerPalette("#F4B5C8", "#F8DAE4", "#c03060", "#F8DAE4EB", "#c03060", "#c03060"),
        )

        val DISABLED_PALETTE = BannerPalette(
            "#D4CFC8", "#E8E5E1", "#a39e97", "#EFEDE9EB", "#a39e97", "#c4bfb8"
        )
    }
}
