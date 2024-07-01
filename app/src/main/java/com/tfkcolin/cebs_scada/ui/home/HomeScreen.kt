package com.tfkcolin.cebs_scada.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.tfkcolin.cebs_scada.util.OK_RESPOND

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    dataList: List<PinValue>,
    onGetPinEvent: (PinValue) -> PinEvent? = { null },
    gridCellNumber: Int = 4,
    mapKeys: List<MapKey>,
    onButtonClicked: (String) -> Unit,
    isHidden: Boolean = false,
    onHideChanged: () -> Unit = {},
    testText: String,
    onTestTextChanged: (String) -> Unit,
){
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .animateContentSize()
    ) {
        TextConsole(
            modifier = Modifier.weight(1f),
            dataList = dataList,
            onGetPinEvent = onGetPinEvent
        )
        TestLayout(
            text = testText,
            onTextChanged = onTestTextChanged,
            onButtonClicked = {
                onButtonClicked(testText)
            }
        )
        HideAbleButtonLayout(
            mapKeys = mapKeys,
            onClick = onButtonClicked,
            isHidden = isHidden,
            onHideChanged = onHideChanged,
            gridCellNumber = gridCellNumber
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    CEBS_SCADATheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            HomeScreen(
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
                mapKeys = keys.map { s ->
                    MapKey(
                        key = s,
                        cmd = ""
                    )
                },
                onButtonClicked = {},
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
                },
                testText = "2002555",
                onTestTextChanged = {}
            )
        }
    }
}

