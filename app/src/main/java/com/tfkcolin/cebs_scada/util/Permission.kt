package com.tfkcolin.cebs_scada.util

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher

fun requestPermission(
    context: Context,
    launcher: ActivityResultLauncher<String>,
    permission: String,
    onShowExplanatoryUi: (String) -> Unit
){
    when {
        (context as ComponentActivity)
            .shouldShowRequestPermissionRationale(permission) -> {
            onShowExplanatoryUi(permission)
        }
        else -> {
            launcher.launch(permission)
        }
    }
}