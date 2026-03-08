package com.barrita.android.mainapp.app.view.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.barrita.android.mainapp.app.ActionsProviderImpl
import com.barrita.android.mainapp.app.BuildConfig
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.actions.contract.HomeActions
import com.barrita.android.mainapp.app.actions.view.HomeActionAdapter
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityHomeBinding
import com.barrita.android.mainapp.app.util.launchActivity
import com.barrita.android.mainapp.app.view.storeproductslist.StoreProductsListActivity

class HomeActivity : AppCompatActivity() {

    private val binding: PointMainappDemoAppActivityHomeBinding by lazy {
        PointMainappDemoAppActivityHomeBinding.inflate(layoutInflater)
    }

    private val actionAdapter: HomeActionAdapter by lazy {
        HomeActionAdapter(::handlerActionItem)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.run { setContentView(root) }
        actionAdapter.submitList(ActionsProviderImpl.getActions(this@HomeActivity))
        setRecyclerView()
        getVersionName()
    }

    private fun getVersionName() {
        val versionName = BuildConfig.VERSION_NAME
        binding.pointMainappDemoAppVersion.text =
            getString(R.string.point_mainapp_demo_app_version_name, versionName)
    }

    private fun setRecyclerView() {
        binding.rvActions.apply {
            layoutManager = LinearLayoutManager(
                this@HomeActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = actionAdapter
        }
    }

    private fun handlerActionItem(action: HomeActions) {
        when (action) {
            is HomeActions.LaunchActivity -> launchActivity(action.activity)
            is HomeActions.LaunchBtUi -> action.actionManager.bluetoothUiSettings.launch(this@HomeActivity)
            is HomeActions.LaunchStoreProductsList -> startActivity(
                Intent(this, StoreProductsListActivity::class.java)
                    .putExtra(StoreProductsListActivity.EXTRA_STORE_ID, action.storeId)
            )
        }
    }
}
