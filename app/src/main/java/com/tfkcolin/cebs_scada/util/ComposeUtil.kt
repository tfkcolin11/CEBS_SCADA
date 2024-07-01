package com.tfkcolin.cebs_scada.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
fun SystemBroadCastReceiver(
    systemAction: String,
    onSystemEvent: (intent: Intent?) -> Unit
){
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(newValue = onSystemEvent)
    DisposableEffect(context, systemAction){
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                currentOnSystemEvent(p1)
            }
        }
        context.registerReceiver(broadcast, intentFilter)
        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}

/*
    val sent = "SMS_SENT"
    val delivered = "SMS_DELIVERED"
    val sentPI = PendingIntent.getBroadcast(context, 0, Intent(sent), 0)
    val deliveredPI = PendingIntent.getBroadcast(context, 0, Intent(delivered), 0)
    context.registerReceiver(
        object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                when(resultCode){
                    Activity.RESULT_OK -> {/*sms sent*/}
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {}
                    SmsManager.RESULT_ERROR_NO_SERVICE -> {}
                    SmsManager.RESULT_ERROR_NULL_PDU -> {}
                    SmsManager.RESULT_ERROR_RADIO_OFF -> {}
                }
            }
        },
        IntentFilter(sent)
    )
    context.registerReceiver(
        object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                when(resultCode){
                    Activity.RESULT_OK -> {/*sms delivered*/}
                    Activity.RESULT_CANCELED -> {/*sms not delivered*/}
                }
            }
        },
        IntentFilter(delivered)
    )
    manager.sendTextMessage(
                number,
                null,
                msg,
                sentPI,
                deliveredPI
            )
 */
