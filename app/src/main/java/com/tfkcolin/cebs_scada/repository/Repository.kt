package com.tfkcolin.cebs_scada.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.tfkcolin.cebs_scada.data.DeviceData
import com.tfkcolin.cebs_scada.data.MapKey
import com.tfkcolin.cebs_scada.data.MapKeyDao
import com.tfkcolin.cebs_scada.data.PinEvent
import com.tfkcolin.cebs_scada.data.PinEventDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

enum class BluetoothState {
    STATE_NONE,
    STATE_DISCONNECTED,
    STATE_CONNECTING, // now initiating an outgoing connection
    STATE_CONNECTED, // now connected to a remote device
    STATE_DISCONNECTING,
    STATE_LISTEN
}

/*
val uuids = listOf(
    UUID.fromString("e8e10f95-1a70-4b27-9ccf-02010264e9c9"),
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
)*/
val HC06_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

private const val NAME_SECURE = "BluetoothChatSecure"
private const val NAME_INSECURE = "BluetoothChatInsecure"

// Unique UUID for this application
private val MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

data class BluetoothFunctionHolder(
    val write: (out: ByteArray?, onError: (String) -> Unit) -> Unit,
    val stop: () -> Unit,
    val startDiscovery: (context: Context) -> Unit,
    val stopDiscovery: (context: Context) -> Unit,
    val connect: suspend (device: BluetoothDevice,
                          context: Context,
                          uuid: UUID,
                          onConnectionFailed: (String) -> Unit) -> Unit
)

class Repository private constructor() {
    private lateinit var pinEventDao: PinEventDao
    private lateinit var mapKeyDao: MapKeyDao

    var bluetoothData = mutableStateListOf<DeviceData>()
        private set
    var smsData = mutableStateListOf<DeviceData>()
        private set

    var bluetoothFunctionHolder: BluetoothFunctionHolder? = null
        private set

    var state: State<BluetoothState> = mutableStateOf(BluetoothState.STATE_NONE)
        private set
    var devices = mutableStateListOf<BluetoothDevice>()
        private set
    var boundedDevices = mutableStateOf<List<BluetoothDevice>>(listOf())
        private set
    var isBluetoothEnabled = mutableStateOf(false)
        private set

    companion object {
        private val INSTANCE = Repository()
        fun getInstance() = INSTANCE
    }

    fun setBluetoothFunctionHolder(holder: BluetoothFunctionHolder){
        bluetoothFunctionHolder = holder
    }
    fun setState(newData: MutableState<BluetoothState>){
        state = newData
    }
    fun setSourceDevices(newDevices: SnapshotStateList<BluetoothDevice>){
        devices = newDevices
    }
    fun setSourceBoundedDevices(bounded: MutableState<List<BluetoothDevice>>){
        boundedDevices = bounded
    }
    fun setIsBluetoothEnable(enable: MutableState<Boolean>){
        isBluetoothEnabled = enable
    }
    fun addBluetoothData(dataReceived: SnapshotStateList<DeviceData>) {
        bluetoothData = dataReceived
    }
    fun addSmsData(dataReceived: SnapshotStateList<DeviceData>) {
        smsData = dataReceived
    }

    fun removeBluetoothData() {
        bluetoothData = mutableStateListOf()
    }
    fun removeSmsData() {
        smsData = mutableStateListOf()
    }
    fun removeBluetoothFunctionHolder(){
        bluetoothFunctionHolder = null
    }
    fun removeState(){
        state = mutableStateOf(BluetoothState.STATE_NONE)
    }
    fun removeSourceDevices(){
        devices = mutableStateListOf()
    }
    fun removeBoundedDevices(){
        boundedDevices = mutableStateOf(listOf())
    }
    fun removeIsBluetoothEnable(){
        isBluetoothEnabled = mutableStateOf(false)
    }

    fun initializeDatabase(dao: PinEventDao, keyDao: MapKeyDao){
        pinEventDao = dao
        mapKeyDao = keyDao
    }

    fun getPinEvents() = pinEventDao.getPinEvents()

    suspend fun insertMapKeys(maps: List<MapKey>){
        withContext(Dispatchers.IO){
            mapKeyDao.insertMapKey(maps)
        }
    }
    suspend fun updateMapKey(mapKey: MapKey){
        withContext(Dispatchers.IO){
            mapKeyDao.updateKeyMapping(mapKey)
        }
    }
    suspend fun deleteMapKey(mapKey: MapKey){
        withContext(Dispatchers.IO){
            mapKeyDao.deleteMapKey(mapKey)
        }
    }
    suspend fun updateMapKeys(mapKey: List<MapKey>){
        withContext(Dispatchers.IO){
            mapKeyDao.updateKeyMappings(mapKey)
        }
    }
    suspend fun deleteMapKeys(mapKey: List<MapKey>){
        withContext(Dispatchers.IO){
            mapKeyDao.deleteMapKeys(mapKey)
        }
    }
    fun getKeyMaps() = mapKeyDao.getKeyMaps()
    suspend fun deletePinEvent(pinEvent: PinEvent){
        withContext(Dispatchers.IO){
            pinEventDao.deletePinEvent(pinEvent)
        }
    }
    suspend fun deletePinEvents(pinEvents: List<PinEvent>){
        withContext(Dispatchers.IO){
            pinEventDao.deletePinEvents(pinEvents)
        }
    }
    suspend fun insertPinEvent(pinEvent: PinEvent): Long{
        return withContext(Dispatchers.IO){
            pinEventDao.insertPinEvent(pinEvent)
        }
    }
    suspend fun insertPinEvents(pinEvents: List<PinEvent>): List<Long>{
        return withContext(Dispatchers.IO){
            pinEventDao.insertPinEvents(pinEvents)
        }
    }
    suspend fun updatePinEvents(pinEvents: List<PinEvent>){
        withContext(Dispatchers.IO){
            pinEventDao.updatePinEvents(pinEvents)
        }
    }
    suspend fun updatePinEvent(pinEvent: PinEvent){
        withContext(Dispatchers.IO){
            pinEventDao.updatePinEvent(pinEvent)
        }
    }
}