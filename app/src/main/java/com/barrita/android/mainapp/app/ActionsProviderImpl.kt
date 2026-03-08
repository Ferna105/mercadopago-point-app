package com.barrita.android.mainapp.app

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.mercadolibre.android.point_integration_sdk.nativesdk.MPManager
import com.barrita.android.mainapp.app.actions.contract.ActionsProvider
import com.barrita.android.mainapp.app.actions.contract.HomeActions
import com.barrita.android.mainapp.app.actions.model.ActionModel
import com.barrita.android.mainapp.app.view.bluetooth.BluetoothTestActivity
import com.barrita.android.mainapp.app.view.bluetooth.printer.PrinterTestActivity
import com.barrita.android.mainapp.app.view.camera.LaunchScannerActivity
import com.barrita.android.mainapp.app.view.info.SmartInfoActivity
import com.barrita.android.mainapp.app.view.payment.launcher.PaymentLauncherActivity
import com.barrita.android.mainapp.app.view.payment.result.PaymentStatusApprovedActivity
import com.barrita.android.mainapp.app.view.printer.PrinterBitmapActivity
import com.barrita.android.mainapp.app.view.printer.PrinterCustomTagActivity
import com.barrita.android.mainapp.app.view.refunds.RefundsActivity
import com.barrita.android.mainapp.app.view.storeproductslist.StoreProductsListActivity

object ActionsProviderImpl : ActionsProvider {
    override fun getActions(context: Context): List<ActionModel> {
        return listOf(
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_btn_store_products_list),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_ic_options
                ),
                action = HomeActions.LaunchStoreProductsList(storeId = "1")
            ),
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_go_to_payment),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_ic_payments
                ),
                action = HomeActions.LaunchActivity(PaymentLauncherActivity::class.java)
            ),
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_lab_text_go_to_payment_status),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_ic_payments
                ),
                action = HomeActions.LaunchActivity(PaymentStatusApprovedActivity::class.java)
            ),
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_button_bluetooth_tools),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_ic_bluetooth
                ),
                action = HomeActions.LaunchActivity(BluetoothTestActivity::class.java)
            ),
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_button_bluetooth_ui_settings),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_ic_bluetooth
                ),
                action = HomeActions.LaunchBtUi(MPManager)
            ),
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_button_refunds),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_ic_payments
                ),
                action = HomeActions.LaunchActivity(RefundsActivity::class.java)
            ),
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_home_print_label),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_black_ic_print
                ),
                action = HomeActions.LaunchActivity(PrinterTestActivity::class.java)
            ),
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_home_printer_bitmap),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_black_ic_print
                ),
                action = HomeActions.LaunchActivity(PrinterBitmapActivity::class.java)
            ),
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_home_printer_custom_tag),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_black_ic_print
                ),
                action = HomeActions.LaunchActivity(PrinterCustomTagActivity::class.java)
            ),
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_cammera_scanner_main_title),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_ic_qr_code
                ),
                action = HomeActions.LaunchActivity(LaunchScannerActivity::class.java)
            ),
            ActionModel(
                title = context.getString(R.string.point_mainapp_demo_app_smart_info_main_title),
                icon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.point_mainapp_demo_app_ic_info
                ),
                action = HomeActions.LaunchActivity(SmartInfoActivity::class.java)
            ),
        )
    }
}
