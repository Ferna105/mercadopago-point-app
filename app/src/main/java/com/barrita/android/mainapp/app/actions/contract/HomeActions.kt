package com.barrita.android.mainapp.app.actions.contract

import com.mercadolibre.android.point_integration_sdk.nativesdk.MPManager
import com.barrita.android.mainapp.app.view.home.HomeActivity

sealed class HomeActions {
   class LaunchActivity(val activity: Class<*>) : HomeActions()
   class LaunchBtUi(val actionManager: MPManager) : HomeActions()
   class LaunchStoreProductsList(val storeId: String) : HomeActions()
}
