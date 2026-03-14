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
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityHomeBinding
import com.google.gson.Gson
import com.barrita.android.mainapp.app.view.login.LoginActivity
import com.barrita.android.mainapp.app.view.storeproductslist.StoreProductsListActivity
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private val binding: PointMainappDemoAppActivityHomeBinding by lazy {
        PointMainappDemoAppActivityHomeBinding.inflate(layoutInflater)
    }

    private val storesAdapter = StoresAdapter { store ->
        navigateToStoreProducts(store)
    }

    private val storesService = NetworkDependencyProvider.storesService
    private val authService = NetworkDependencyProvider.authService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUserInfo()
        setupRecyclerView()
        loadStores()
    }

    private fun setupUserInfo() {
        val userEmail = SessionManager.getUserEmail(this)
        binding.pointMainappDemoAppUserEmail.text = userEmail ?: ""
    }

    private fun setupRecyclerView() {
        binding.pointMainappDemoAppStoresRecycler.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = storesAdapter
        }
    }

    private fun loadStores() {
        val accessToken = SessionManager.getAccessToken(this)
        // #region agent log
        Log.d("DEBUG_0b902f", "[H1] accessToken isNull=${accessToken == null}, length=${accessToken?.length ?: 0}")
        // #endregion
        if (accessToken == null) {
            // #region agent log
            Log.d("DEBUG_0b902f", "[H1] CONFIRMED: Token is null, redirecting to login")
            // #endregion
            redirectToLogin()
            return
        }

        showLoading()
        fetchStores(accessToken, isRetry = false)
    }

    private fun fetchStores(accessToken: String, isRetry: Boolean) {
        lifecycleScope.launch {
            try {
                // #region agent log
                Log.d("DEBUG_0b902f", "[FETCH] Making request to stores endpoint, isRetry=$isRetry")
                // #endregion
                val response = storesService.getStores("Bearer $accessToken")
                // #region agent log
                Log.d("DEBUG_0b902f", "[FETCH] Response code=${response.code()}, isSuccessful=${response.isSuccessful}")
                // #endregion

                when {
                    response.isSuccessful -> {
                        val body = response.body()
                        // #region agent log
                        Log.d("DEBUG_0b902f", "[FETCH] body isNull=${body == null}, dataSize=${body?.data?.size ?: -1}")
                        // #endregion
                        val stores = body?.data ?: emptyList()
                        if (stores.isEmpty()) {
                            showEmpty()
                        } else {
                            // #region agent log
                            Log.d("DEBUG_0b902f", "SUCCESS: Loaded ${stores.size} stores")
                            // #endregion
                            showStores(stores)
                        }
                    }
                    response.code() == 401 && !isRetry -> {
                        // #region agent log
                        Log.d("DEBUG_0b902f", "[REFRESH] Got 401, attempting token refresh...")
                        // #endregion
                        tryRefreshToken()
                    }
                    else -> {
                        // #region agent log
                        val errorBody = response.errorBody()?.string()
                        Log.d("DEBUG_0b902f", "[FETCH] HTTP error ${response.code()}, errorBody=$errorBody")
                        // #endregion
                        showError(getString(R.string.point_mainapp_demo_app_home_error))
                    }
                }
            } catch (e: Exception) {
                // #region agent log
                Log.d("DEBUG_0b902f", "[FETCH] EXCEPTION: ${e.javaClass.simpleName}: ${e.message}")
                // #endregion
                showError(getString(R.string.point_mainapp_demo_app_home_error))
            }
        }
    }

    private fun tryRefreshToken() {
        val refreshToken = SessionManager.getRefreshToken(this)
        if (refreshToken == null) {
            // #region agent log
            Log.d("DEBUG_0b902f", "[REFRESH] No refresh token available, redirecting to login")
            // #endregion
            redirectToLogin()
            return
        }

        lifecycleScope.launch {
            try {
                // #region agent log
                Log.d("DEBUG_0b902f", "[REFRESH] Calling refresh endpoint...")
                // #endregion
                val response = authService.refreshToken(RefreshRequest(refreshToken))
                // #region agent log
                Log.d("DEBUG_0b902f", "[REFRESH] Response code=${response.code()}, isSuccessful=${response.isSuccessful}")
                // #endregion

                if (response.isSuccessful) {
                    val body = response.body()
                    val newAccessToken = body?.accessToken
                    val newRefreshToken = body?.refreshToken

                    if (newAccessToken != null && newRefreshToken != null) {
                        // #region agent log
                        Log.d("DEBUG_0b902f", "[REFRESH] SUCCESS: Got new tokens, retrying stores request")
                        // #endregion
                        SessionManager.updateTokens(this@HomeActivity, newAccessToken, newRefreshToken)
                        fetchStores(newAccessToken, isRetry = true)
                    } else {
                        // #region agent log
                        Log.d("DEBUG_0b902f", "[REFRESH] FAILED: Response body missing tokens")
                        // #endregion
                        redirectToLogin()
                    }
                } else {
                    // #region agent log
                    val errorBody = response.errorBody()?.string()
                    Log.d("DEBUG_0b902f", "[REFRESH] FAILED: HTTP ${response.code()}, errorBody=$errorBody")
                    // #endregion
                    redirectToLogin()
                }
            } catch (e: Exception) {
                // #region agent log
                Log.d("DEBUG_0b902f", "[REFRESH] EXCEPTION: ${e.javaClass.simpleName}: ${e.message}")
                // #endregion
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
            pointMainappDemoAppProgress.visibility = View.VISIBLE
            pointMainappDemoAppStoresRecycler.visibility = View.GONE
            pointMainappDemoAppEmptyMessage.visibility = View.GONE
            pointMainappDemoAppErrorMessage.visibility = View.GONE
        }
    }

    private fun showStores(stores: List<Store>) {
        binding.apply {
            pointMainappDemoAppProgress.visibility = View.GONE
            pointMainappDemoAppStoresRecycler.visibility = View.VISIBLE
            pointMainappDemoAppEmptyMessage.visibility = View.GONE
            pointMainappDemoAppErrorMessage.visibility = View.GONE
        }
        storesAdapter.submitList(stores)
    }

    private fun showEmpty() {
        binding.apply {
            pointMainappDemoAppProgress.visibility = View.GONE
            pointMainappDemoAppStoresRecycler.visibility = View.GONE
            pointMainappDemoAppEmptyMessage.visibility = View.VISIBLE
            pointMainappDemoAppErrorMessage.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        binding.apply {
            pointMainappDemoAppProgress.visibility = View.GONE
            pointMainappDemoAppStoresRecycler.visibility = View.GONE
            pointMainappDemoAppEmptyMessage.visibility = View.GONE
            pointMainappDemoAppErrorMessage.visibility = View.VISIBLE
            pointMainappDemoAppErrorMessage.text = message
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
