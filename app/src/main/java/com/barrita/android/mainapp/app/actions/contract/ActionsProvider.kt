package com.barrita.android.mainapp.app.actions.contract

import android.content.Context
import com.barrita.android.mainapp.app.actions.model.ActionModel

interface ActionsProvider {
    fun getActions(context: Context): List<ActionModel>
}
