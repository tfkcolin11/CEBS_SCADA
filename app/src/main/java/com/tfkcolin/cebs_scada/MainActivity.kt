package com.tfkcolin.cebs_scada

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.tfkcolin.cebs_scada.data.AppMode
import com.tfkcolin.cebs_scada.data.MapKey
import com.tfkcolin.cebs_scada.data.PinEvent
import com.tfkcolin.cebs_scada.data.PinEventDatabase
import com.tfkcolin.cebs_scada.repository.BluetoothDeviceReceiver
import com.tfkcolin.cebs_scada.repository.BluetoothFunctionHolder
import com.tfkcolin.cebs_scada.repository.HC06_UUID
import com.tfkcolin.cebs_scada.repository.Repository
import com.tfkcolin.cebs_scada.repository.SmsReceiver
import com.tfkcolin.cebs_scada.ui.AppViewModel
import com.tfkcolin.cebs_scada.ui.theme.CEBS_SCADATheme
import com.tfkcolin.cebs_scada.util.KeyMapPreferences
import com.tfkcolin.cebs_scada.util.requestPermission
import kotlinx.coroutines.launch

val keys = arrayOf(
    "1", "2", "3", "A",
    "4", "5", "6", "B",
    "7", "8", "9", "C",
    "F1", "F2", "F3", "F4"
)

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothReceiver: BluetoothDeviceReceiver
    private lateinit var smsReceiver: SmsReceiver
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repository.getInstance().initializeDatabase(
            PinEventDatabase
                .getInstance(this)
                .pinEventDao,
            PinEventDatabase
                .getInstance(this)
                .mapKeyDao
        )
        val adapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        val mode = KeyMapPreferences(this).appMode()
        val useEncryption = KeyMapPreferences(this).encryptionMode()
        viewModel.mode.value = AppMode.entries.toTypedArray()[if(mode == -1) 0 else mode]
        viewModel.useEncryption.value = useEncryption

        bluetoothReceiver = BluetoothDeviceReceiver.getInstance(adapter, this)
        lifecycle.addObserver(bluetoothReceiver)
        smsReceiver = SmsReceiver()
        lifecycle.addObserver(smsReceiver)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            viewModel.connectPermissionGranted.value = ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            viewModel.scanPermissionGranted.value = ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        }
        else {
            viewModel.connectPermissionGranted.value = true
            viewModel.scanPermissionGranted.value = true
        }
        viewModel.sendSmsPermissionGranted.value = ActivityCompat
            .checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        viewModel.receiveSmsPermissionGranted.value = ActivityCompat
            .checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED

        // Generated code
        enableEdgeToEdge()
        setContent {
            CEBS_SCADATheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        if(viewModel.mode.value == AppMode.BLUETOOTH){
            registerForActivityResult(
                ActivityResultContracts
                    .StartActivityForResult()
            ) {

            }.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        Repository.getInstance().setBluetoothFunctionHolder(
            BluetoothFunctionHolder(
                write = bluetoothReceiver::write,
                stop = bluetoothReceiver::stop,
                startDiscovery = bluetoothReceiver::startDiscovery,
                stopDiscovery = bluetoothReceiver::stopDiscovery,
                connect = bluetoothReceiver::connect
            )
        )
        Repository.getInstance().addBluetoothData(bluetoothReceiver.dataReceived)
        Repository.getInstance().addSmsData(smsReceiver.dataReceived)
        Repository.getInstance().setState(bluetoothReceiver.state)
        Repository.getInstance().setIsBluetoothEnable(bluetoothReceiver.isBluetoothEnabled)
        Repository.getInstance().setSourceBoundedDevices(bluetoothReceiver.boundedDevices)
        Repository.getInstance().setSourceDevices(bluetoothReceiver.devices)
    }

    override fun onStop() {
        super.onStop()
        Repository.getInstance().removeBluetoothFunctionHolder()
        Repository.getInstance().removeBluetoothData()
        Repository.getInstance().removeSmsData()
        Repository.getInstance().removeState()
        Repository.getInstance().removeBoundedDevices()
        Repository.getInstance().removeIsBluetoothEnable()
        Repository.getInstance().removeSourceDevices()
    }
}

@Composable
fun App(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val dataList by viewModel.proceedData//.observeAsState(listOf())
    val mapKey by viewModel.mapKey.observeAsState(listOf())
    val pinEvents by viewModel.pinEvents.observeAsState(listOf())

    val mapKeyTemp = remember { mutableStateListOf<MapKey>() }
    val pinEventTemp = remember { mutableStateListOf<PinEvent>() }
    var testText by remember { mutableStateOf("")}

    val navState by navController.currentBackStackEntryAsState()

    val context = LocalContext.current
    (context as ComponentActivity).lifecycle.addObserver(viewModel)
    val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    val bluetoothState by remember { BluetoothDeviceReceiver.getInstance(adapter, context).state }

    val keyMapPreferences = KeyMapPreferences(context)

    var homeHideKeyboardOn by remember { mutableStateOf(false) }
    var dialogVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    var isAdmin by remember { mutableStateOf(false)}

    var showExplanatoryDialog by remember { mutableStateOf(false)}

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            viewModel.needPermissionToActivate.value = true
        }
    )

    val requestPermission: (String?) -> Unit = {
        it?.let{
            requestPermission(
                context = context,
                launcher = launcher,
                permission = it,
                onShowExplanatoryUi = { showExplanatoryDialog = true }
            )
        }
    }

    BackHandler(enabled = state.drawerState.isOpen) {
        scope.launch {
            state.drawerState.close()
        }
    }

    LaunchedEffect(viewModel.needPermissionToActivate.value){
        if(viewModel.needPermissionToActivate.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                viewModel.connectPermissionGranted.value = ActivityCompat
                    .checkSelfPermission(
                        context,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                viewModel.scanPermissionGranted.value = ActivityCompat
                    .checkSelfPermission(
                        context,
                        android.Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
            }
            viewModel.sendSmsPermissionGranted.value = ActivityCompat
                .checkSelfPermission(
                    context,
                    android.Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            viewModel.receiveSmsPermissionGranted.value = ActivityCompat
                .checkSelfPermission(
                    context,
                    android.Manifest.permission.RECEIVE_SMS
                ) == PackageManager.PERMISSION_GRANTED
            viewModel.needPermissionToActivate.value = false
        }
    }

    LaunchedEffect(Unit){
        if(keyMapPreferences.isFirstRun()){
            Log.i("first", "enter init keys")
            viewModel.insertMapKeys(
                keys.map { s ->
                    MapKey(
                        key = s,
                        cmd = ""
                    )
                }
            )
            keyMapPreferences.setFirstRun()
            keyMapPreferences.setPwd("999999")
        }
        focusManager.clearFocus()

        val lastDevice = keyMapPreferences.lastDeviceAddress()
        if(keyMapPreferences.autoConnect() &&
            lastDevice != ""
        ){
            val device = (context
                .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                .adapter.getRemoteDevice(lastDevice)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val permission = when {
                    ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED -> {
                        android.Manifest.permission.BLUETOOTH_SCAN
                    }
                    ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED -> {
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    }
                    else -> {
                        null
                    }
                }
                requestPermission(permission)
            }//request permission if needed
            if(viewModel.scanPermissionGranted.value
                && viewModel.connectPermissionGranted.value
                && viewModel.mode.value == AppMode.BLUETOOTH
            ) {
                viewModel.connect(
                    device = device,
                    context = context,
                    HC06_UUID
                ) {
                    scope.launch {
                        state.snackbarHostState.showSnackbar(it)
                    }
                }
            }
        }
    }

    var errorMessage: String? by remember { mutableStateOf(null)}
    LaunchedEffect(errorMessage){
        errorMessage?.let {
            scope.launch {
                state.snackbarHostState.showSnackbar(errorMessage!!)
            }
            errorMessage = null
        }
    }

    BackHandler(enabled = showExplanatoryDialog) {
        showExplanatoryDialog = false
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CEBS_SCADATheme {
        App(AppViewModel())
    }
}