package com.tfkcolin.cebs_scada.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat

@Composable
fun DeviceListDialog(
    onDismissRequest: () -> Unit,
    boundedDevices: List<BluetoothDevice>,
    onBoundedItemClicked: (BluetoothDevice) -> Unit,
    devices: List<BluetoothDevice>,
    onDeviceItemClicked: (BluetoothDevice) -> Unit,
){
    val allDevices = arrayListOf<BluetoothDevice>()
    boundedDevices.forEach {
        allDevices.add(it)
    }
    devices.forEach {
        allDevices.add(it)
    }
    Dialog(onDismissRequest = { /*TODO*/ }) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Dialog(
                onDismissRequest = onDismissRequest,
            ) {
                LazyColumn{
                    item {
                        Text(
                            modifier = Modifier.padding(5.dp),
                            text = "Device List",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(allDevices){ device ->
                        if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                            && ActivityCompat.checkSelfPermission(
                                LocalContext.current,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Text(
                                text = "We don't have permission to display this data",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else {
                            DeviceItem(
                                modifier = Modifier.clickable {
                                    if(device.bondState == BluetoothDevice.BOND_BONDED)
                                        onBoundedItemClicked(device)
                                    else
                                        onDeviceItemClicked(device)
                                },
                                device = device
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
fun DeviceItem(
    modifier: Modifier = Modifier,
    device: BluetoothDevice
){
    Column(
        modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        val text = if(device.bondState == BluetoothDevice.BOND_BONDED)
            "Bounded"
        else
            "unBounded"
        Text(text = device.name, style = MaterialTheme.typography.titleSmall)
        Text(text = device.address, style = MaterialTheme.typography.labelSmall)
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}