package com.barrita.android.mainapp.app.view.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        setupToolbar()
        setupUserInfo()
        setupRecyclerView()
        loadStores()
    }

    private fun setupToolbar() {
        binding.pointMainappDemoAppMyStoresToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupUserInfo() {
        val userEmail = SessionManager.getUserEmail(this)
        binding.pointMainappDemoAppMyStoresUserEmail.text = userEmail ?: ""
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
                Log.d("DEBUG_0b902f", "[FETCH] Making request to stores endpoint, isRetry=$isRetry")
                val response = storesService.getStores("Bearer $accessToken")
                Log.d("DEBUG_0b902f", "[FETCH] Response code=${response.code()}, isSuccessful=${response.isSuccessful}")

                when {
                    response.isSuccessful -> {
                        val body = response.body()
                        val stores = body?.data ?: emptyList()
                        if (stores.isEmpty()) {
                            showEmpty()
                        } else {
                            Log.d("DEBUG_0b902f", "SUCCESS: Loaded ${stores.size} stores")
                            showStores(stores)
                        }
                    }
                    response.code() == 401 && !isRetry -> {
                        Log.d("DEBUG_0b902f", "[REFRESH] Got 401, attempting token refresh...")
                        tryRefreshToken()
                    }
                    else -> {
                        showError(getString(R.string.point_mainapp_demo_app_home_error))
                    }
                }
            } catch (e: Exception) {
                Log.d("DEBUG_0b902f", "[FETCH] EXCEPTION: ${e.javaClass.simpleName}: ${e.message}")
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
        }
    }

    private fun showStores(stores: List<Store>) {
        binding.apply {
            pointMainappDemoAppMyStoresProgress.visibility = View.GONE
            pointMainappDemoAppMyStoresRecycler.visibility = View.VISIBLE
            pointMainappDemoAppMyStoresEmptyMessage.visibility = View.GONE
            pointMainappDemoAppMyStoresErrorMessage.visibility = View.GONE
        }
        storesAdapter.submitList(stores)
    }

    private fun showEmpty() {
        binding.apply {
            pointMainappDemoAppMyStoresProgress.visibility = View.GONE
            pointMainappDemoAppMyStoresRecycler.visibility = View.GONE
            pointMainappDemoAppMyStoresEmptyMessage.visibility = View.VISIBLE
            pointMainappDemoAppMyStoresErrorMessage.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        binding.apply {
            pointMainappDemoAppMyStoresProgress.visibility = View.GONE
            pointMainappDemoAppMyStoresRecycler.visibility = View.GONE
            pointMainappDemoAppMyStoresEmptyMessage.visibility = View.GONE
            pointMainappDemoAppMyStoresErrorMessage.visibility = View.VISIBLE
            pointMainappDemoAppMyStoresErrorMessage.text = message
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
