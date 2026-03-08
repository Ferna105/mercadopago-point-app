package com.barrita.android.mainapp.app.actions.model

import android.graphics.drawable.Drawable
import com.barrita.android.mainapp.app.actions.contract.HomeActions

data class ActionModel(
    val title: String,
    val icon: Drawable?,
    val action: HomeActions,
)
