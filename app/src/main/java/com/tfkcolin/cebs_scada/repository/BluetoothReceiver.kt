package com.tfkcolin.cebs_scada.repository

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.internal.commonToUtf8String
import com.tfkcolin.cebs_scada.data.AppMode
import com.tfkcolin.cebs_scada.data.DeviceData
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

private const val NAME_SECURE = "BluetoothChatSecure"
private const val NAME_INSECURE = "BluetoothChatInsecure"
private val MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

class BluetoothDeviceReceiver private constructor(
    private val mAdapter: BluetoothAdapter,
    private val context: Context
) : BroadcastReceiver(), DefaultLifecycleObserver {
    private var mmSocket: MutableState<BluetoothSocket?> = mutableStateOf(null)
    private var mmInputStream: InputStream? = null
    private var mmOutputStream: OutputStream? = null

    val state = mutableStateOf(BluetoothState.STATE_NONE)
    val devices = mutableStateListOf<BluetoothDevice>()
    val dataReceived = mutableStateListOf<DeviceData>()
    val isBluetoothEnabled = mutableStateOf(false)

    private var mmServerSocket: BluetoothServerSocket? = null

    companion object {
        private var INSTANCE: BluetoothDeviceReceiver? = null
        fun getInstance(adapter: BluetoothAdapter, context: Context): BluetoothDeviceReceiver {
            if (INSTANCE == null) {
                INSTANCE = BluetoothDeviceReceiver(adapter, context)
            }
            return INSTANCE!!
        }
    }

    @SuppressLint("MissingPermission")
    val boundedDevices = mutableStateOf(getBondedDevices())

    private fun getBondedDevices(): List<BluetoothDevice> {
        return if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        )
            listOf()
        else
            mAdapter.bondedDevices?.toList() ?: listOf()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            when (it.action) {
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> Unit
                BluetoothAdapter.ACTION_STATE_CHANGED -> handleBluetoothStateChange(it)
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> boundedDevices.value = getBondedDevices()
                BluetoothDevice.ACTION_FOUND -> handleDeviceFound(it)
                BluetoothDevice.ACTION_ACL_CONNECTED -> updateState(BluetoothState.STATE_CONNECTED)
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> updateState(BluetoothState.STATE_DISCONNECTED)
                BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> updateState(BluetoothState.STATE_DISCONNECTING)
            }
        }
    }

    private fun handleBluetoothStateChange(intent: Intent) {
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        isBluetoothEnabled.value = state == BluetoothAdapter.STATE_ON
    }

    private fun handleDeviceFound(intent: Intent) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Use the new type-safe method for API level 33 and above
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            // Use the deprecated method for API levels below 33 (with @SuppressWarnings)
            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        }
        device?.let { if (!devices.contains(it)) devices.add(it) }
    }

    private fun updateState(newState: BluetoothState) {
        state.value = newState
        Log.i("BluetoothDeviceReceiver", "State updated to $newState")
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun listen(
        adapter: BluetoothAdapter,
        onMessage: (String) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        var tmp: BluetoothServerSocket? = null

        kotlin.runCatching {
            tmp = adapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE)
        }.onFailure { e ->
            Log.e("BluetoothDeviceReceiver", "Socket listen() failed", e)
        }

        mmServerSocket = tmp
        updateState(BluetoothState.STATE_LISTEN)

        while (state.value != BluetoothState.STATE_CONNECTED) {
            kotlin.runCatching {
                socket = mmServerSocket?.accept()
            }.onFailure { e ->
                Log.e("BluetoothDeviceReceiver", "Socket accept() failed", e)
            }

            socket?.let {
                mmSocket.value = it
                when (state.value) {
                    BluetoothState.STATE_LISTEN, BluetoothState.STATE_CONNECTING -> listen(
                        device = it.remoteDevice,
                        onMessage = onMessage
                    )
                    else -> it.closeSafely()
                }
            }
        }
    }

    private suspend fun listen(device: BluetoothDevice, onMessage: (String) -> Unit) {
        val inputStream: InputStream?
        val outputStream: OutputStream?
        try {
            inputStream = mmSocket.value?.inputStream
            outputStream = mmSocket.value?.outputStream
        } catch (e: IOException) {
            Log.e("BluetoothDeviceReceiver", "Input/Output streams failed to create", e)
            onMessage("Input/Output streams failed to create: ${e.message}")
            return
        }

        mmInputStream = inputStream
        mmOutputStream = outputStream

        updateState(BluetoothState.STATE_CONNECTED)

        val buffer = ByteArray(1024)
        var size: Int

        withContext(Dispatchers.IO) {
            while (state.value == BluetoothState.STATE_CONNECTED) {
                kotlin.runCatching {
                    size = mmInputStream?.read(buffer) ?: 0
                    val message = buffer.commonToUtf8String().substring(0 until size)
                    updateDataReceived(device.address, message)
                }.onFailure {
                    onMessage("Connection lost: ${it.message}")
                    updateState(BluetoothState.STATE_DISCONNECTED)
                    Log.e("BluetoothDeviceReceiver", "Connection lost", it)
                }
            }
        }
    }

    private fun updateDataReceived(address: String, message: String) {
        val existingData = dataReceived.firstOrNull { it.address == address }
        if (existingData == null) {
            dataReceived.add(
                DeviceData(
                    address = address,
                    messages = arrayListOf(message),
                    mode = AppMode.BLUETOOTH,
                    time = Calendar.getInstance().timeInMillis
                )
            )
        } else {
            dataReceived.remove(existingData)
            dataReceived.add(existingData.apply { messages.add(message) })
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connect(
        device: BluetoothDevice,
        uuid: UUID,
        onConnectionFailed: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        var tmp: BluetoothSocket? = null

        kotlin.runCatching {
            tmp = device.createInsecureRfcommSocketToServiceRecord(uuid)
        }.onFailure {
            onConnectionFailed("Socket creation failed: ${it.message}")
            return@withContext
        }

        mmSocket.value = tmp

        mAdapter.cancelDiscovery()

        kotlin.runCatching {
            mmSocket.value?.connect()
        }.onFailure {
            mmSocket.value?.closeSafely()
            onConnectionFailed("Unable to connect to device: ${it.message}")
            return@withContext
        }

        onConnectionFailed("Connected")
        listen(device, onConnectionFailed)
    }

    private fun BluetoothSocket.closeSafely() {
        kotlin.runCatching { close() }
            .onFailure { Log.e("BluetoothDeviceReceiver", "Could not close unwanted socket", it) }
    }

    fun stop() {
        mmSocket.value?.closeSafely()
        mmSocket.value = null
        mmOutputStream = null
        mmInputStream = null
        updateState(BluetoothState.STATE_NONE)
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun startDiscovery() {
        if (mAdapter.isDiscovering) mAdapter.cancelDiscovery()
        mAdapter.startDiscovery()
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun stopDiscovery() {
        if (mAdapter.isDiscovering) mAdapter.cancelDiscovery()
    }

    fun write(out: ByteArray?, onError: (String) -> Unit) {
        if (state.value == BluetoothState.STATE_CONNECTED) {
            try {
                mmOutputStream?.write(out)
                Log.i("BluetoothDeviceReceiver", "Data sent")
            } catch (e: IOException) {
                onError(e.message ?: "Unable to write")
            }
        } else {
            onError("Please connect to a device")
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        (owner as ContextWrapper).unregisterReceiver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        registerAllReceivers(owner as ContextWrapper)
    }

    private fun registerAllReceivers(contextWrapper: ContextWrapper) {
        val intentFilters = listOf(
            BluetoothDevice.ACTION_FOUND,
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
            BluetoothDevice.ACTION_BOND_STATE_CHANGED,
            BluetoothAdapter.ACTION_STATE_CHANGED,
            BluetoothDevice.ACTION_ACL_CONNECTED,
            BluetoothDevice.ACTION_ACL_DISCONNECTED,
            BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED
        ).map { IntentFilter(it) }

        intentFilters.forEach { contextWrapper.registerReceiver(this, it) }
    }
}