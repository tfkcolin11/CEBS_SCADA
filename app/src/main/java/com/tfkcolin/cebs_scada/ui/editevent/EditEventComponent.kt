package com.tfkcolin.cebs_scada.ui.editevent

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfkcolin.cebs_scada.data.PinEvent
import com.tfkcolin.cebs_scada.ui.editkey.MapItem
import com.tfkcolin.cebs_scada.ui.theme.CEBS_SCADATheme

@Composable
fun PinEventConfig(
    modifier: Modifier = Modifier,
    pins: List<PinEvent>,
    temps: List<PinEvent> = listOf(),
    state: LazyListState = rememberLazyListState(),
    onPinChanged: (PinEvent, String, String) -> Unit,
    onHiddenButtonClicked: (PinEvent, String) -> Unit,
    onAddEventClicked: (PinEvent) -> Unit,
    onDeletePinEvent: (PinEvent) -> Unit
){
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 10.dp),
        state = state
    ){
        items(pins){ pin ->
            PinValueConfigItem(
                pinEvent = pin,
                temp = temps.firstOrNull { it.mcPinId == pin.mcPinId },
                onPinEventChanged = { newPin, s, s2 ->
                    onPinChanged(newPin, s, s2)
                },
                onHiddenButtonClicked = onHiddenButtonClicked,
                onAddEventClicked = onAddEventClicked,
                onDeletePinEvent = {onDeletePinEvent(pin)}
            )
        }
    }
}

@Composable
fun PinValueConfigItem(
    modifier: Modifier = Modifier,
    pinEvent: PinEvent,
    temp: PinEvent?,
    onPinEventChanged: (PinEvent, String, String) -> Unit,
    onHiddenButtonClicked: (PinEvent, String) -> Unit,
    onAddEventClicked: (PinEvent) -> Unit,
    onDeletePinEvent: () -> Unit
){
    var keyValueText by remember { mutableStateOf("") }
    var editVisible by remember { mutableStateOf(false) }
    val height by animateDpAsState(
        targetValue =
        if (editVisible)
            TextFieldDefaults.MinHeight + 10.dp + ButtonDefaults.MinHeight
        else
            0.dp
    )
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .padding(bottom = 5.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondary
                .copy(alpha = .3f)
        ) {
            Column {
                Row {
                    Text(
                        modifier = Modifier
                            .padding(10.dp),
                        text = "pin: ${pinEvent.mcPinId}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        modifier = Modifier.padding(horizontal = 5.dp),
                        onClick = {
                            editVisible = true
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ) {
                        Text(text = "Add Event")
                    }
                    IconButton(
                        modifier = Modifier.padding(end = 5.dp),
                        onClick = onDeletePinEvent,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete pin events"
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .height(height),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier
                            .padding(5.dp)
                    ) {
                        Text(text = "Value: ")
                        TextField(
                            value = keyValueText,
                            onValueChange = { keyValueText = it },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {focusManager.clearFocus()}
                            )
                        )
                    }
                    Row{
                        TextButton(
                            onClick = {
                                editVisible = false
                                focusManager.clearFocus()
                            }
                        ) {
                            Text(text = "Cancel")
                        }
                        TextButton(
                            onClick = {
                                if(keyValueText != "" && keyValueText !in pinEvent.eventsMap.keys) {
                                    val out = hashMapOf<String, String>()
                                    out.putAll(pinEvent.eventsMap)
                                    out[keyValueText] = ""
                                    onAddEventClicked(pinEvent.copy(eventsMap = out))
                                    editVisible = false
                                    focusManager.clearFocus()
                                }
                            }
                        ) {
                            Text(text = "Add")
                        }
                    }
                }
            }
        }
        pinEvent.eventsMap.forEach(
            action = { entry ->
                //var keyText by remember { mutableStateOf(entry.key) }
                var valueText by remember { mutableStateOf(entry.value) }
                LaunchedEffect(temp){
                    temp?.let {
                        valueText = it.eventsMap[entry.key]!!
                    }
                }
                MapItem(
                    keyText = entry.key,
                    onKeyTextChanged = {
                        /*keyText = it
                        onPinEventChanged(
                            PinEvent(
                                mcPinId = pinEvent.mcPinId,
                                eventsMap = pinEvent.eventsMap
                                    .mapKeys { keyEntry ->
                                        when(entry.key){
                                            keyEntry.key -> {
                                                it
                                            }
                                            else -> {
                                                keyEntry.key
                                            }
                                        }
                                    }
                            )
                        )*/
                    },
                    cmdText = valueText,
                    onCmdTextChanged = {
                        valueText = it
                        onPinEventChanged(
                            PinEvent(
                                mcPinId = pinEvent.mcPinId,
                                eventsMap = pinEvent.eventsMap
                                    .mapValues { keyEntry ->
                                        when(entry.value){
                                            keyEntry.value -> {
                                                if(entry.key == keyEntry.key)
                                                    it
                                                else
                                                    keyEntry.value
                                            }
                                            else -> {
                                                keyEntry.value
                                            }
                                        }
                                    }
                            ),
                            entry.key,
                            it
                        )
                    },
                    isKeyEditable = false,
                    keyLabelText = "Value",
                    cmdLabelText = "Event",
                    onHiddenButtonClicked = {
                        val out = hashMapOf<String, String>()
                        out.putAll(pinEvent.eventsMap)
                        out.remove(entry.key)
                        val pin = PinEvent(
                            mcPinId = pinEvent.mcPinId,
                            eventsMap = out
                        )
                        onHiddenButtonClicked(pin, entry.key)
                    }
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PinEventConfigItemPreview(){
    CEBS_SCADATheme {
        Surface {
            PinEventConfig(
                pins = listOf(
                    PinEvent(
                        mcPinId = "1",
                        eventsMap = mapOf(
                            Pair("true", "door opened"),
                            Pair("false", "door closed")
                        )
                    ),
                    PinEvent(
                        mcPinId = "2",
                        eventsMap = mapOf(
                            Pair("true", "door is opening")
                        )
                    ),
                    PinEvent(
                        mcPinId = "A2",
                        eventsMap = mapOf(
                            Pair("100", "motor low speed"),
                            Pair("512", "motor mid speed"),
                            Pair("1024", "motor full speed")
                        )
                    )
                ),
                onPinChanged = {_,_,_ ->},
                onHiddenButtonClicked = {_,_ ->},
                onAddEventClicked = {},
                onDeletePinEvent = {}
            )
        }
    }
}