package com.tfkcolin.cebs_scada.ui

import android.bluetooth.*
import android.content.*
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.tfkcolin.cebs_scada.data.*
import com.tfkcolin.cebs_scada.repository.Repository
import com.tfkcolin.cebs_scada.util.Protocol
import com.tfkcolin.cebs_scada.util.decrypt
import com.tfkcolin.cebs_scada.util.iv
import com.tfkcolin.cebs_scada.util.key
import java.util.*

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
class AppViewModel: ViewModel(), DefaultLifecycleObserver {
    // Member fields
    private val repository = Repository.getInstance()
    val state = repository.state
    var mode = mutableStateOf(AppMode.BLUETOOTH)
    var useEncryption = mutableStateOf(false)

    private fun dataReceived() = when(mode.value){
        AppMode.BLUETOOTH -> repository.bluetoothData
        else -> repository.smsData
    }

    val proceedData = derivedStateOf {
        val out = mutableListOf<PinValue>()
        dataReceived().forEach { received ->
            received.messages.forEach { msg ->
                out.add(
                    Protocol
                        .getInstance()
                        .decomposeResponse(
                            if(useEncryption.value)
                                decrypt("AES/CBC/NoPadding", msg, key, iv)
                            else
                                msg
                        )
                )
            }
        }
        out.toList()
    }

    //Bluetooth data
    fun devices() = repository.devices
    fun boundedDevices() = repository.boundedDevices
    fun isBluetoothEnabled() = repository.isBluetoothEnabled

    //general data
    val pinEvents = repository.getPinEvents()
    val mapKey = repository.getKeyMaps()

    var connectPermissionGranted = mutableStateOf(false)
    var scanPermissionGranted = mutableStateOf(false)
    var sendSmsPermissionGranted = mutableStateOf(false)
    var receiveSmsPermissionGranted = mutableStateOf(false)
    val needPermissionToActivate = mutableStateOf(false) // enable us to update the above variable

    fun getPinEvent(pin: PinValue) = pinEvents.value?.find {
        when(pin){
            is AnalogPinValue -> {
                it.mcPinId == "A${pin.pin}"
                        && it.eventsMap.containsKey("${pin.value}")
            }
            is DigitalPinValue -> {
                it.mcPinId == "${pin.pin}" &&
                        it.eventsMap.containsKey("${pin.value}")
            }
            else ->{
                false
            }
        }
    }

    suspend fun insertPinEvent(pinEvent: PinEvent) = repository
        .insertPinEvent(pinEvent)
    suspend fun insertPinEvents(pinEvents: List<PinEvent>) = repository
        .insertPinEvents(pinEvents)
    suspend fun deletePinEvent(pinEvent: PinEvent) = repository
        .deletePinEvent(pinEvent)
    suspend fun deletePinEvents(pinEvents: List<PinEvent>) = repository
        .deletePinEvents(pinEvents)
    suspend fun updatePinEvents(pinEvents: List<PinEvent>) = repository
        .updatePinEvents(pinEvents)
    suspend fun updatePinEvent(pinEvent: PinEvent) = repository
        .updatePinEvent(pinEvent)

    suspend fun insertMapKeys(maps: List<MapKey>) = repository
        .insertMapKeys(maps)
    suspend fun updateMapKey(mapKey: MapKey) = repository
        .updateMapKey(mapKey)
    suspend fun updateMapKeys(mapKey: List<MapKey>) = repository
        .updateMapKeys(mapKey)
    suspend fun deleteMapKey(mapKey: MapKey) = repository
        .deleteMapKey(mapKey)
    suspend fun deleteMapKeys(mapKey: List<MapKey>) = repository
        .deleteMapKeys(mapKey)

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun startDiscovery() = repository
        .bluetoothFunctionHolder
        ?.startDiscovery
        ?.invoke() ?: Unit

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun stopDiscovery() = repository
        .bluetoothFunctionHolder
        ?.stopDiscovery
        ?.invoke() ?: Unit

    @RequiresPermission(
        anyOf = [
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_SCAN"
        ]
    )
    suspend fun connect(device: BluetoothDevice,
                uuid: UUID,
                onConnectionFailed: (String) -> Unit) = repository
        .bluetoothFunctionHolder
        ?.connect
        ?.invoke(
            device,
            uuid,
            onConnectionFailed
        ) ?: Unit

    /*@RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun listen(adapter: BluetoothAdapter) = repository
        .listen(adapter)*/

    fun stop() = repository
        .bluetoothFunctionHolder
        ?.stop
        ?.invoke() ?: Unit

    fun write(data: String,
              onError: (String) -> Unit){
        repository
            .bluetoothFunctionHolder
            ?.write
            ?.invoke(data.toByteArray()){
                onError(it)
            }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        stop()
    }
}
