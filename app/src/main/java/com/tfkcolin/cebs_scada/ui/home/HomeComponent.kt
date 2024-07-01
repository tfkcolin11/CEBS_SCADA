package com.tfkcolin.cebs_scada.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfkcolin.cebs_scada.data.AnalogPinValue
import com.tfkcolin.cebs_scada.data.CommonPinValue
import com.tfkcolin.cebs_scada.data.DigitalPinValue
import com.tfkcolin.cebs_scada.data.MapKey
import com.tfkcolin.cebs_scada.data.PinEvent
import com.tfkcolin.cebs_scada.data.PinValue
import com.tfkcolin.cebs_scada.data.StatusPinValue
import com.tfkcolin.cebs_scada.ui.keys
import com.tfkcolin.cebs_scada.ui.theme.CEBS_SCADATheme
import com.tfkcolin.cebs_scada.util.INVALID_CMD
import com.tfkcolin.cebs_scada.util.INVALID_PWD
import com.tfkcolin.cebs_scada.util.OK_RESPOND
import java.text.DateFormat
import java.util.Date

@Composable
fun TextConsole(
    modifier: Modifier = Modifier,
    dataList: List<PinValue>,
    onGetPinEvent: (PinValue) -> PinEvent? = { null }
){
    val state = rememberLazyListState()
    val format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
    LaunchedEffect(dataList){
        if(dataList.isNotEmpty())
            state.scrollToItem(dataList.lastIndex)
    }
    Column(
        modifier = modifier
    ) {
        Text(
            modifier = Modifier
                .padding(bottom = 5.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = .5f),
            text = "Event console",
            style = MaterialTheme.typography.titleSmall
        )
        LazyColumn(
            modifier = modifier,
            state = state
        ) {
            items(dataList){ pinValue ->
                var text = ""
                var pinId = ""
                var time = ""
                when(pinValue){
                    is AnalogPinValue -> {
                        val pinTest = onGetPinEvent(pinValue)
                        pinId = "A${pinValue.pin}"
                        time = format.format(Date(pinValue.time))
                        text = if (pinTest == null)
                            "pin: A${pinValue.pin}, value: ${pinValue.value}"
                        else
                            pinTest.eventsMap["${pinValue.value}"]!!
                    }
                    is DigitalPinValue -> {
                        val pinTest = onGetPinEvent(pinValue)
                        pinId = "A${pinValue.pin}"
                        time = format.format(Date(pinValue.time))
                        text = if (pinTest == null)
                            "pin: ${pinValue.pin}, value: ${pinValue.value}"
                        else
                            pinTest.eventsMap["${pinValue.value}"]!!
                    }
                    is StatusPinValue -> {
                        time = format.format(Date(pinValue.time))
                        text = when(pinValue.value){
                            INVALID_CMD -> {
                                "Incorrect Command"
                            }
                            INVALID_PWD -> {
                                "Incorrect password"
                            }
                            OK_RESPOND -> {
                                "OK: action perform"
                            }
                            else -> {
                                "Incorrect Input"
                            }
                        }
                    }
                    is CommonPinValue -> {
                        time = format.format(Date(pinValue.time))
                        text = pinValue.value
                    }
                }
                PinData(
                    pinId = pinId,
                    time = time,
                    event = text
                )
            }
        }
    }
}

@Composable
fun PinData(
    modifier: Modifier = Modifier,
    pinId: String,
    time: String,
    event: String
){
    Card(
        modifier = modifier
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(5.dp)
        ) {
            if(pinId != "")
                Text(text = pinId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            Text(text = time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

            Text(
                text = event,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify
            )
        }
    }
}

@Composable
fun ButtonLayout(
    modifier: Modifier = Modifier,
    mapKeys: List<MapKey>,
    onClick: (String) -> Unit,
    gridCellNumber: Int = 4
){
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(gridCellNumber)
    ){
        items(mapKeys){
            TextButton(
                modifier = Modifier
                    .padding(horizontal = 5.dp, vertical = 5.dp),
                onClick = { onClick(it.cmd) },
            ) {
                Text(text = it.key)
            }
        }
    }
}

@Composable
fun HideAbleButtonLayout(
    modifier: Modifier = Modifier,
    mapKeys: List<MapKey>,
    onClick: (String) -> Unit,
    isHidden: Boolean = false,
    onHideChanged: () -> Unit = {},
    gridCellNumber: Int = 4
){
    val padding by animateDpAsState(
        targetValue = if (isHidden) 10.dp else 0.dp,
        label = "hide_able_bt_padding"
    )
    Box(modifier = modifier
        .fillMaxWidth()
        .animateContentSize()){
        AnimatedVisibility(visible = !isHidden) {
            ButtonLayout(
                modifier = Modifier.padding(
                    top = ButtonDefaults.MinHeight + 10.dp
                ),
                mapKeys = mapKeys,
                onClick = onClick,
                gridCellNumber = gridCellNumber
            )
        }
        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    end = 10.dp,
                    bottom = padding
                ),
            onClick = onHideChanged
        ) {
            Icon(imageVector = if(isHidden)
                Icons.Filled.KeyboardArrowUp
            else
                Icons.Filled.KeyboardArrowDown,
                contentDescription = if(isHidden)
                    "show buttons"
                else
                    "hide buttons",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun TestLayout(
    modifier: Modifier = Modifier,
    text: String,
    onTextChanged: (String) -> Unit,
    onButtonClicked: () -> Unit
){
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = modifier.padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            modifier = Modifier
                .height(TextFieldDefaults.MinHeight),
            onClick = onButtonClicked
        ) {
            Text(text = "Send")
        }
        Spacer(modifier = Modifier.width(30.dp))
        TextField(
            value = text,
            onValueChange = onTextChanged,
            placeholder = { Text(text = "Enter test command")},
            label = { Text(text = "Test")},
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TextConSolePreview(){
    CEBS_SCADATheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TextConsole(
                dataList = listOf(
                    AnalogPinValue(
                        pin = 0,
                        value = 100
                    ),
                    DigitalPinValue(
                        pin = 12,
                        value = false
                    ),
                    StatusPinValue(
                        pin = 1,
                        value = OK_RESPOND,
                    ),
                    CommonPinValue(
                        value = "i wanted to open the door but something happen please help me."
                    )
                ),
                onGetPinEvent = {
                    listOf(
                        PinEvent(
                            mcPinId = "A0",
                            eventsMap = mapOf(Pair("100", "motor middle speed"))
                        ),
                        PinEvent(
                            mcPinId = "12",
                            eventsMap = mapOf(Pair("false", "door open"))
                        )
                    ).find { pin ->
                        when(it){
                            is AnalogPinValue -> {
                                pin.mcPinId == "A${it.pin}"
                                        && pin.eventsMap.containsKey("${it.value}")
                            }
                            is DigitalPinValue -> {
                                pin.mcPinId == "${it.pin}" &&
                                        pin.eventsMap.containsKey("${it.value}")
                            }
                            else ->{
                                false
                            }
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ButtonLayoutPreview(){
    CEBS_SCADATheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            HideAbleButtonLayout(
                mapKeys = keys.map { s ->
                    MapKey(
                        key = s,
                        cmd = ""
                    )
                },
                onClick = {}
            )
        }
    }
}