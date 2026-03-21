package com.barrita.android.mainapp.app.view.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.data.NetworkDependencyProvider
import com.barrita.android.mainapp.app.data.SessionManager
import com.barrita.android.mainapp.app.data.dto.RefreshRequest
import com.barrita.android.mainapp.app.data.dto.Store
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityMyStoresBinding
import com.barrita.android.mainapp.app.view.login.LoginActivity
import com.barrita.android.mainapp.app.view.storeproductslist.StoreProductsListActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MyStoresActivity : AppCompatActivity() {

    private val binding: PointMainappDemoAppActivityMyStoresBinding by lazy {
        PointMainappDemoAppActivityMyStoresBinding.inflate(layoutInflater)
    }

    private val storesAdapter = StoresAdapter { store ->
        navigateToStoreProducts(store)
    }

    private val storesService = NetworkDependencyProvider.storesService
    private val authService = NetworkDependencyProvider.authService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupRecyclerView()
        loadStores()
    }

    private fun setupRecyclerView() {
        binding.pointMainappDemoAppMyStoresRecycler.apply {
            layoutManager = LinearLayoutManager(this@MyStoresActivity)
            adapter = storesAdapter
        }
    }

    private fun loadStores() {
        val accessToken = SessionManager.getAccessToken(this)
        if (accessToken == null) {
            redirectToLogin()
            return
        }
        showLoading()
        fetchStores(accessToken, isRetry = false)
    }

    private fun fetchStores(accessToken: String, isRetry: Boolean) {
        lifecycleScope.launch {
            try {
                val response = storesService.getStores("Bearer $accessToken")

                when {
                    response.isSuccessful -> {
                        val body = response.body()
                        val stores = body?.data ?: emptyList()
                        if (stores.isEmpty()) {
                            showEmpty()
                        } else {
                            showStores(stores)
                        }
                    }
                    response.code() == 401 && !isRetry -> {
                        tryRefreshToken()
                    }
                    else -> {
                        showError(getString(R.string.point_mainapp_demo_app_home_error))
                    }
                }
            } catch (e: Exception) {
                showError(getString(R.string.point_mainapp_demo_app_home_error))
            }
        }
    }

    private fun tryRefreshToken() {
        val refreshToken = SessionManager.getRefreshToken(this)
        if (refreshToken == null) {
            redirectToLogin()
            return
        }
        lifecycleScope.launch {
            try {
                val response = authService.refreshToken(RefreshRequest(refreshToken))
                if (response.isSuccessful) {
                    val body = response.body()
                    val newAccessToken = body?.accessToken
                    val newRefreshToken = body?.refreshToken
                    if (newAccessToken != null && newRefreshToken != null) {
                        SessionManager.updateTokens(this@MyStoresActivity, newAccessToken, newRefreshToken)
                        fetchStores(newAccessToken, isRetry = true)
                    } else {
                        redirectToLogin()
                    }
                } else {
                    redirectToLogin()
                }
            } catch (e: Exception) {
                redirectToLogin()
            }
        }
    }

    private fun redirectToLogin() {
        SessionManager.clearSession(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showLoading() {
        binding.apply {
            pointMainappDemoAppMyStoresProgress.visibility = View.VISIBLE
            pointMainappDemoAppMyStoresRecycler.visibility = View.GONE
            pointMainappDemoAppMyStoresEmptyMessage.visibility = View.GONE
            pointMainappDemoAppMyStoresErrorMessage.visibility = View.GONE
            pointMainappDemoAppMyStoresBadges.visibility = View.GONE
        }
    }

    private fun showStores(stores: List<Store>) {
        val total = stores.size
        val active = stores.count { it.status == "active" }
        val inactive = total - active

        binding.apply {
            pointMainappDemoAppMyStoresProgress.visibility = View.GONE
            pointMainappDemoAppMyStoresRecycler.visibility = View.VISIBLE
            pointMainappDemoAppMyStoresEmptyMessage.visibility = View.GONE
            pointMainappDemoAppMyStoresErrorMessage.visibility = View.GONE

            pointMainappDemoAppMyStoresBadges.visibility = View.VISIBLE
            pointMainappDemoAppBadgeTotal.text =
                getString(R.string.point_mainapp_demo_app_stores_badge_total, total)

            if (active > 0) {
                pointMainappDemoAppBadgeActive.visibility = View.VISIBLE
                pointMainappDemoAppBadgeActive.text = if (active == 1)
                    getString(R.string.point_mainapp_demo_app_stores_badge_active, active)
                else
                    getString(R.string.point_mainapp_demo_app_stores_badge_active_plural, active)
            } else {
                pointMainappDemoAppBadgeActive.visibility = View.GONE
            }

            if (inactive > 0) {
                pointMainappDemoAppBadgeInactive.visibility = View.VISIBLE
                pointMainappDemoAppBadgeInactive.text = if (inactive == 1)
                    getString(R.string.point_mainapp_demo_app_stores_badge_inactive, inactive)
                else
                    getString(R.string.point_mainapp_demo_app_stores_badge_inactive_plural, inactive)
            } else {
                pointMainappDemoAppBadgeInactive.visibility = View.GONE
            }
        }
        storesAdapter.submitList(stores)
    }

    private fun showEmpty() {
        binding.apply {
            pointMainappDemoAppMyStoresProgress.visibility = View.GONE
            pointMainappDemoAppMyStoresRecycler.visibility = View.GONE
            pointMainappDemoAppMyStoresEmptyMessage.visibility = View.VISIBLE
            pointMainappDemoAppMyStoresErrorMessage.visibility = View.GONE
            pointMainappDemoAppMyStoresBadges.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        binding.apply {
            pointMainappDemoAppMyStoresProgress.visibility = View.GONE
            pointMainappDemoAppMyStoresRecycler.visibility = View.GONE
            pointMainappDemoAppMyStoresEmptyMessage.visibility = View.GONE
            pointMainappDemoAppMyStoresErrorMessage.visibility = View.VISIBLE
            pointMainappDemoAppMyStoresErrorMessage.text = message
            pointMainappDemoAppMyStoresBadges.visibility = View.GONE
        }
    }

    private fun navigateToStoreProducts(store: Store) {
        val storeJson = Gson().toJson(store)
        startActivity(
            Intent(this, StoreProductsListActivity::class.java)
                .putExtra(StoreProductsListActivity.EXTRA_STORE_ID, store.id)
                .putExtra(StoreProductsListActivity.EXTRA_STORE_JSON, storeJson)
        )
    }
}
