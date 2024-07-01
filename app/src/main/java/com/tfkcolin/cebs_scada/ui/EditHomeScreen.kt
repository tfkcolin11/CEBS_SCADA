package com.tfkcolin.cebs_scada.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tfkcolin.cebs_scada.ui.editkey.MapItem
import com.tfkcolin.cebs_scada.data.MapKey
import com.tfkcolin.cebs_scada.util.KeyMapPreferences
import java.util.regex.Pattern

@Composable
fun EditHomeScreen(
    modifier: Modifier = Modifier,
    mapKeys: List<MapKey>,
    temps: List<MapKey>,
    onKeySelectionChanged: (MapKey) -> Unit,
    onHiddenButtonClicked: (MapKey) -> Unit,
    onClearTemp: () -> Unit
){
    val context = LocalContext.current
    val preferences = KeyMapPreferences(context)
    val (autoConnect, setAutoConnect) = remember { mutableStateOf(preferences.autoConnect())}
    var columnNumber by remember {
        mutableStateOf(preferences.keyboardColumn().toString())
    }
    var selectAll by remember {
        mutableStateOf(mapKeys.filter{ it.selected }.size == mapKeys.size)
    }
    DisposableEffect(Unit){
        onDispose {
            onClearTemp()
        }
    }
    LazyColumn(
        modifier = modifier
            .padding(10.dp)
    ){
        item {
            Text(
                text = "Keyboard",
                style = MaterialTheme.typography.headlineLarge
            )
            HorizontalDivider(modifier = Modifier.padding(10.dp))
        }
        item {
            Text(
                text = "How many column do you want to have (max 6) for your home keyboard ?",
                style = MaterialTheme.typography.labelSmall
            )
            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pattern = Pattern.compile("""\d""")
                Text(text = "Number")
                Spacer(modifier = Modifier.width(50.dp))
                TextField(
                    value = columnNumber,
                    onValueChange = {
                        columnNumber = it
                        if(it.length == 1) {
                            preferences.setKeyboardColumn(
                                try {
                                    val nb = columnNumber.toInt()
                                    if (nb >= 6)
                                        6
                                    else
                                        nb
                                } catch (e: NumberFormatException) {
                                    4
                                }
                            )
                        }
                    },
                    isError = !pattern.matcher(columnNumber).matches()
                )
            }
        }
        item {
            Text(
                text = "SMS",
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider(modifier = Modifier.padding(10.dp))
        }
        item {
            Text(
                text = "Phone number",
                style = MaterialTheme.typography.titleSmall
            )
            HorizontalDivider(modifier = Modifier.padding(10.dp))
            Text(
                text = "Enter the phone number with whom you want to communicate",
                style = MaterialTheme.typography.labelSmall
            )
            val pattern = Pattern.compile("""[+]?\d+""")
            var number by remember { mutableStateOf("")}
            LaunchedEffect(Unit){
                number = preferences.phoneNumber() ?: ""
            }
            TextField(
                value = number,
                onValueChange = {
                    number = it
                    preferences.setPhoneNumber(number)
                },
                isError = !pattern.matcher(number).matches()
            )
        }
        item {
            Text(
                text = "Bluetooth",
                style = MaterialTheme.typography.titleSmall
            )
            HorizontalDivider(modifier = Modifier.padding(10.dp))
        }
        item {
            Text(
                text = "Connect automatically to the last device ?",
                style = MaterialTheme.typography.labelSmall
            )
            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Auto connect")
                Spacer(modifier = Modifier.width(50.dp))
                Checkbox(
                    checked = autoConnect,
                    onCheckedChange = {
                        setAutoConnect(it)
                        preferences.setAutoConnect(it)
                    }
                )
            }
        }
        item {
            Text(
                text = "Buttons",
                style = MaterialTheme.typography.titleSmall
            )
            HorizontalDivider(modifier = Modifier.padding(10.dp))
        }
        item {
            Text(
                text = "Select the buttons to be shown in the home screen",
                style = MaterialTheme.typography.labelSmall
            )
            Row(
                modifier = Modifier.padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(end = 15.dp),
                    text = if(!selectAll) "Select all" else "Unselect All")
                Checkbox(
                    checked = selectAll,
                    onCheckedChange = {
                        selectAll = it
                        mapKeys.forEach { map ->
                            onKeySelectionChanged(map.copy(selected = it))
                        }
                    }
                )
            }
        }
        items(mapKeys){ map ->
            var checked by remember { mutableStateOf(map.selected)}
            LaunchedEffect(temps, selectAll){
                if(map.id in temps.map { it.id }){
                    checked = temps.find{ it.id == map.id}!!.selected
                }
            }
            CheckableComposable(
                checked = checked,
                onCheckedChanged = {
                    checked = !checked
                    onKeySelectionChanged(map.copy(selected = checked))
                }
            ) {
                MapItem(
                    modifier = Modifier.padding(vertical = 5.dp),
                    keyText = map.key,
                    onKeyTextChanged = {},
                    cmdText = map.cmd,
                    onCmdTextChanged = {},
                    isKeyEditable = false,
                    isCmdEditable = false,
                    cmdIsSingleLine = true,
                    keyIsSingleLine = true,
                    onHiddenButtonClicked = {
                        onHiddenButtonClicked(map)
                    }
                )
            }
        }
    }
}

@Composable
fun CheckableComposable(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChanged: () -> Unit,
    content: @Composable () -> Unit
){
    val padding by animateDpAsState(targetValue = if (checked) 15.dp else 5.dp, label = "check")
    Box(modifier = modifier
        .clickable { onCheckedChanged() }){
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.TopEnd),
            enter = fadeIn(),
            exit = fadeOut(),
            visible = checked
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null
            )
        }
        Box(modifier = Modifier
            .padding(padding)
            .align(Alignment.Center)
        ) {
            content()
        }
    }
}