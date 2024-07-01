package com.tfkcolin.cebs_scada.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat


fun sendSms(
    context: Context,
    number: String,
    msg: String,
    onError: (String?) -> Unit,
    onRequestPermission: () -> Unit
){
    if(
        ActivityCompat
            .checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
    ){
        try {
            val manager =
                context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
            Log.i("home", if(manager != null) "is not null" else "is null")
            manager.sendTextMessage(number, null, msg, null, null)
        }catch (e: Exception){
            onError(e.message ?: "error")//"For some reason you are unable to perform this action."
        }
    }
    else
        onRequestPermission()
}