package com.tfkcolin.cebs_scada.repository

import com.tfkcolin.cebs_scada.data.AppMode
import com.tfkcolin.cebs_scada.data.DeviceData
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.internal.commonToUtf8String
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

private const val NAME_SECURE = "BluetoothChatSecure"
private const val NAME_INSECURE = "BluetoothChatInsecure"

// Unique UUID for this application
private val MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

class BluetoothDeviceReceiver private constructor(
    private val mAdapter: BluetoothAdapter,
    context: Context
) : BroadcastReceiver(), DefaultLifecycleObserver {
    private var mmSocket: MutableState<BluetoothSocket?> = mutableStateOf(null)
    private var mmInputStream: InputStream? = null
    private var mmOutputStream: OutputStream? = null

    val state = mutableStateOf(BluetoothState.STATE_NONE)

    val devices = mutableStateListOf<BluetoothDevice>()

    val dataReceived = mutableStateListOf<DeviceData>()

    companion object{
        private var INSTANCE: BluetoothDeviceReceiver? = null
        fun getInstance(adapter: BluetoothAdapter, context: Context): BluetoothDeviceReceiver{
            if(INSTANCE == null)
                INSTANCE = BluetoothDeviceReceiver(adapter, context)
            return INSTANCE!!
        }
    }

    @SuppressLint("MissingPermission")
    val boundedDevices = mutableStateOf(
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        )
            listOf()
        else
            mAdapter.bondedDevices?.toList() ?: listOf()
    )

    var isBluetoothEnabled = mutableStateOf(false)

    //service
    private var mmServerSocket: BluetoothServerSocket? = null

    override fun onReceive(p0: Context?, p1: Intent?) {
        p1?.let {
            when(it.action){
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED ->{

                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = it.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )
                    when (state) {
                        BluetoothAdapter.STATE_ON -> {
                            isBluetoothEnabled.value = true
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            isBluetoothEnabled.value = false
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED ->{
                    if (ActivityCompat.checkSelfPermission(
                            p0!!,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    )
                        return
                    boundedDevices.value = mAdapter
                        .bondedDevices?.toList() ?: listOf()
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device = it
                        .getParcelableExtra<BluetoothDevice>(
                            BluetoothDevice.EXTRA_DEVICE
                        )
                    if(device != null)
                        devices.add(device)
                }
                BluetoothDevice.ACTION_ACL_CONNECTED ->{
                    state.value = BluetoothState.STATE_CONNECTED
                    Log.i("extra", "state connected")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED ->{
                    state.value = BluetoothState.STATE_DISCONNECTED
                    Log.i("extra", "state disconnected")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED ->{
                    state.value = BluetoothState.STATE_DISCONNECTING
                    Log.i("extra", "state disconnecting")
                }
                else -> {}
            }
            it
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun listen(
        adapter: BluetoothAdapter,
        onMessage: (String) -> Unit = {}
    ){
        withContext(Dispatchers.IO){
            var socket: BluetoothSocket? = null
            var tmp: BluetoothServerSocket? = null
            val mSocketType = "Insecure"
            Log.i("test", "listening")

            // Create a new listening server socket
            kotlin.runCatching {
                tmp = adapter.listenUsingInsecureRfcommWithServiceRecord(
                    NAME_INSECURE,
                    MY_UUID_INSECURE
                )
            } .onFailure { e ->
                Log.e(
                    "test",
                    "Socket Type: " + mSocketType + "listen() failed",
                    e
                )
            }
            withContext(Dispatchers.Main){
                mmServerSocket = tmp
                state.value = BluetoothState.STATE_LISTEN
                Log.e("test", "server socket created")
            }
            // Listen to the server socket if we're not connected
            while (state.value != BluetoothState.STATE_CONNECTED) {
                kotlin.runCatching {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket!!.accept()
                    Log.e("test", "accept a client")
                }.onFailure {
                    Log.e(
                        "test",
                        "Socket Type: " + "accept() failed",
                        it
                    )
                }

                // If a connection was accepted
                if (socket != null) {
                    withContext(Dispatchers.Main){
                        mmSocket.value = socket
                    }
                    when (state.value) {
                        BluetoothState.STATE_LISTEN, BluetoothState.STATE_CONNECTING ->
                            listen(onConnectionFailed = onMessage, device = socket!!.remoteDevice)
                        BluetoothState.STATE_NONE, BluetoothState.STATE_CONNECTED ->
                            try {
                                socket!!.close()
                            } catch (e: IOException) {
                                Log.e(
                                    "test",
                                    "Could not close unwanted socket",
                                    e
                                )
                            }
                        else ->{}
                    }
                }
            }
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    @RequiresPermission(
        anyOf = [
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_SCAN"
        ]
    )
    suspend fun connect(device: BluetoothDevice,
                        context: Context,
                        uuid: UUID,
                        onConnectionFailed: (String) -> Unit) {
        withContext(Dispatchers.IO){
            Log.i("test", "enter connect")
            var tmp: BluetoothSocket? = null
            // Start the thread to connect with the given device
            kotlin.runCatching {
                tmp = device.createInsecureRfcommSocketToServiceRecord(
                    //UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                    uuid
                )
            }.onFailure {
                onConnectionFailed("Socket creation failed")
                Log.i("test", "Socket creation failed")
                return@withContext
            }
            Log.i("test", "Socket create")

            withContext(Dispatchers.Main){
                mmSocket.value = tmp
                //state.value = BluetoothState.STATE_CONNECTING
            }

            (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                .adapter
                .cancelDiscovery()

            var outMessage = ""
            // Make a connection to the BluetoothSocket
            kotlin.runCatching {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.value!!.connect()
            }.onFailure {
                // Close the socket
                kotlin.runCatching {
                    Log.i("test", "closing socket")
                    mmSocket.value!!.close()
                } .onFailure { s->
                    outMessage += "unable to close() " +
                            " socket during connection failure"
                }
                onConnectionFailed("Unable to connect to device : $outMessage")
                Log.i("test", "Unable to connect to device : $outMessage")
                return@withContext
            }
            // Start the connected thread
            Log.i("test", "Start listening")
            onConnectionFailed("Connected")
            listen(device, onConnectionFailed)
        }
    }

    private suspend fun listen(device: BluetoothDevice, onConnectionFailed: (String) -> Unit){
        val inputStream: InputStream?
        val outputStream: OutputStream?
        try {
            inputStream = mmSocket.value!!.inputStream
            outputStream = mmSocket.value!!.outputStream
        }catch (e: IOException){
            Log.e("test", "input out failed to create")
            onConnectionFailed("input out failed to create")
            return
        }
        withContext(Dispatchers.Main){
            if (inputStream!= null)
                mmInputStream = inputStream
            if(outputStream != null)
                mmOutputStream = outputStream
        }

        //state.value = BluetoothState.STATE_CONNECTED
        var size = 0
        withContext(Dispatchers.IO){
            Log.i("test", "listen to data send by other")
            val buffer = ByteArray(1024)
            Log.i("test", "state connected: ${state.value == BluetoothState.STATE_CONNECTED}")
            while (state.value == BluetoothState.STATE_CONNECTED){
                Log.i("test", "enter while")
                kotlin.runCatching {
                    Log.i("test", "waiting for data to read")

                    if(inputStream != null){
                        Log.i("test", "trying to read")
                        // Read from the InputStream
                        size = mmInputStream?.read(buffer) ?: 0
                        // Send the obtained bytes to the UI Activity
                        withContext(Dispatchers.Main){
                            val data = dataReceived.firstOrNull {
                                it.address == device.address
                            }
                            val message = buffer.commonToUtf8String().substring(0 until size)
                            if(data == null)
                                dataReceived.add(
                                    DeviceData(
                                        address = device.address,
                                        messages = arrayListOf(
                                            message
                                        ),
                                        mode = AppMode.BLUETOOTH,
                                        time = Calendar.getInstance().timeInMillis
                                    )
                                )
                            else {
                                dataReceived.remove(data)
                                dataReceived.add(data.apply { messages.add(message) })
                            }
                        }
                    }
                }
                    .onFailure {
                        onConnectionFailed("Disconnected:\nCause: connection lost, ${it.message}")
                        withContext(Dispatchers.Main){
                            state.value = BluetoothState.STATE_DISCONNECTED
                        }
                        Log.i("test", "Disconnected:\nCause: connection lost, ${it.message}")
                        return@onFailure
                    }
            }
            Log.e("test", "stop listening")
        }
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        mmSocket.value?.close()
        mmSocket.value = null
        mmOutputStream = null
        mmInputStream = null
        state.value = BluetoothState.STATE_NONE
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun startDiscovery(context: Context){
        val adapter = (context
            .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter
        if(adapter.isDiscovering)
            adapter.cancelDiscovery()
        adapter.startDiscovery()
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun stopDiscovery(context: Context){
        val adapter = (context
            .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter
        if(adapter.isDiscovering)
            adapter.cancelDiscovery()
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * //@see ConnectedThread.write
     */
    fun write(out: ByteArray?,
              onError: (String) -> Unit) {
        if (state.value == BluetoothState.STATE_CONNECTED) {
            try {
                mmOutputStream?.write(out)
                Log.i("test", "has sent")
            } catch (e: IOException){
                onError(e.message ?: "Unable to write")
            }
        }
        else
            onError("Please connect to a device")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        (owner as ContextWrapper).unregisterReceiver(this)
    }
    // was on start before and worked
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        val list = listOf(
            IntentFilter(BluetoothDevice.ACTION_FOUND),
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED),
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED),
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED),
            IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED),
            IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        )
        list.forEach {
            (owner as ContextWrapper).registerReceiver(this, it)
        }
    }
}