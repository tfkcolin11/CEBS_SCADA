package com.tfkcolin.cebs_scada.repository

import com.tfkcolin.cebs_scada.util.KeyMapPreferences
import android.content.*
import android.provider.Telephony
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.tfkcolin.cebs_scada.data.AppMode
import com.tfkcolin.cebs_scada.data.DeviceData
import java.util.*

class SmsReceiver: BroadcastReceiver(), DefaultLifecycleObserver {
    val dataReceived = mutableStateListOf<DeviceData>()

    override fun onReceive(p0: Context?, p1: Intent?) {
        p1?.let {
            val msg = Telephony.Sms.Intents.getMessagesFromIntent(it)

            for (i in msg.indices){
                val data = dataReceived.firstOrNull { data ->
                    data.address == msg[i].originatingAddress
                }
                val phone = KeyMapPreferences(p0!!).phoneNumber()
                if(data == null && phone == msg[i].originatingAddress)
                    dataReceived.add(
                        DeviceData(
                            address = msg[i].originatingAddress ?: "",
                            messages = arrayListOf(
                                msg[i].messageBody
                            ),
                            mode = AppMode.GSM,
                            time = msg[i].timestampMillis
                        )
                    )
                else if(data != null) {
                    dataReceived.remove(data)
                    dataReceived.add(data.apply { messages.add(msg[i].messageBody) })
                }
            }
            /*dataReceived.forEach { data ->
                Log.i(
                    "receiver",
                    "data: \n ${data.address}:\n${data.messages}\n")
            }*/
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        (owner as ContextWrapper).registerReceiver(
            this,
            IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION).apply {
                priority = 999
            },
        )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        (owner as ContextWrapper).unregisterReceiver(this)
    }
}