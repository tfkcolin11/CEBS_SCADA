package com.tfkcolin.cebs_scada.ui.editevent

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfkcolin.cebs_scada.data.PinEvent
import com.tfkcolin.cebs_scada.ui.theme.CEBS_SCADATheme
import java.util.regex.Pattern

@Composable
fun EditEventScreen(
    modifier: Modifier = Modifier,
    pins: List<PinEvent>,
    state: LazyListState = rememberLazyListState(),
    temps: List<PinEvent> = listOf(),
    onPinEventChanged: (PinEvent, String, String) -> Unit,
    onHiddenButtonClicked: (PinEvent, String) -> Unit,
    onAddEventClicked: (PinEvent) -> Unit,
    onAddPinEvent: (pinName: String) -> Unit,
    onDeletePinEvent: (PinEvent) -> Unit,
    onClearTemp: () -> Unit
){
    Column {
        var pin by remember { mutableStateOf("")}
        val reg = remember { Pattern.compile("^[aA]?+\\d+$")}
        DisposableEffect(Unit){
            onDispose {
                onClearTemp()
            }
        }
        HeaderConfig(
            reg = reg,
            text = pin,
            addButtonText = "Add Pin",
            labelText = "Pin name",
            placeholderText = "ex: 0 or A0 ",
            onTextChanged = { pin = it },
            onCreateClicked = {
                onAddPinEvent(pin.uppercase())
            }
        )
        AnimatedVisibility(visible = pins.isNotEmpty()) {
            PinEventConfig(
                modifier = modifier,
                pins = pins,
                temps = temps,
                state = state,
                onPinChanged = onPinEventChanged,
                onHiddenButtonClicked = onHiddenButtonClicked,
                onAddEventClicked = onAddEventClicked,
                onDeletePinEvent = onDeletePinEvent
            )
        }
        AnimatedVisibility(visible = pins.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
            ){
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    text = buildAnnotatedString {
                        append("No event found.\nPlease click on ")
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = .8f),
                                fontWeight = FontWeight.Medium
                            )
                        ){
                            append("\"Add Event\"")
                        }
                        append(" at the top\nTo create one.")
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun HeaderConfig(
    modifier: Modifier = Modifier,
    reg: Pattern? = null,
    isConfirmable: Boolean = false,
    isSecret: Boolean = false,
    autoFocus: Boolean = true,
    confirmText: String = "",
    confirmTextLabel: String = "",
    createText: String = "Create",
    buttonColor: Color = MaterialTheme.colorScheme.onSurface,
    onConFirmTextChanged: (String) -> Unit = {},
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = .7f),
    text: String,
    addButtonText: String,
    labelText: String,
    onTextChanged: (String) -> Unit,
    placeholderText: String,
    onCreateClicked: () -> Unit
){
    var clicked by remember{ mutableStateOf(false) }
    val height = remember {
        if (isConfirmable)
            TextFieldDefaults.MinHeight.times(2) + 10.dp
        else
            TextFieldDefaults.MinHeight
    }
    val initHeight by animateDpAsState(targetValue = if(clicked)
        height + ButtonDefaults.MinHeight + 20.dp
    else
        0.dp, label = "height"
    )
    val addHeight by animateDpAsState(targetValue = if(!clicked)
        ButtonDefaults.MinHeight + 10.dp
    else
        0.dp, label = "dp"
    )
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = modifier,
        color = color
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .height(addHeight)
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        clicked = true
                        if(autoFocus)
                            focusManager.moveFocus(FocusDirection.Next)
                    }
                ) {
                    Text(
                        text = addButtonText,
                        color = buttonColor
                    )
                }
            }
            Column(
                modifier = Modifier
                    .height(initHeight)
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                LazyVerticalGrid(columns = GridCells.Fixed(2)){
                    item {
                        Text(
                            text = labelText,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                    item {
                        TextField(
                            value = text,
                            onValueChange = onTextChanged,
                            colors = TextFieldDefaults.colors(
                                cursorColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .8f)
                            ),
                            isError = !(reg?.matcher(text)?.matches() ?: true),
                            placeholder = { Text(text = placeholderText)},
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {focusManager.clearFocus()}
                            ),
                            visualTransformation = if(isSecret)
                                PasswordVisualTransformation()
                        else
                            VisualTransformation.None
                        )
                    }
                    if(isConfirmable){
                        item {
                            Text(text = confirmTextLabel, color = MaterialTheme.colorScheme.onSurface)
                        }
                        item {
                            TextField(
                                modifier = Modifier.padding(vertical = 5.dp),
                                value = confirmText,
                                onValueChange = onConFirmTextChanged,
                                colors = TextFieldDefaults.colors(
                                    cursorColor = MaterialTheme.colorScheme.onSurface,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .8f)
                                ),
                                isError = !(reg?.matcher(text)?.matches() ?: true),
                                placeholder = { Text(text = placeholderText)},
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {focusManager.clearFocus()}
                                ),
                                visualTransformation = if(isSecret)
                                    PasswordVisualTransformation()
                                else
                                    VisualTransformation.None
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            clicked = false
                            focusManager.clearFocus()
                        }
                    ) {
                        Text(
                            text = "Cancel",
                            color = buttonColor
                        )
                    }
                    TextButton(
                        modifier = Modifier.padding(end = 5.dp),
                        onClick = {
                            if(reg?.matcher(text)?.matches() != false) {
                                onCreateClicked()
                                clicked = false
                                focusManager.clearFocus()
                                onTextChanged("")
                                onConFirmTextChanged("")
                            }
                        }
                    ) {
                        Text(
                            text = createText,
                            color = buttonColor
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun EditEventPreview(){
    CEBS_SCADATheme {
        Surface{
            EditEventScreen(
                pins = listOf(
                    PinEvent(
                        mcPinId = "1",
                        eventsMap = mapOf(
                            "true" to "Door Open",
                            "false" to "Door Closed"
                        )
                    ),
                    PinEvent(
                        mcPinId = "A1",
                        eventsMap = mapOf(
                            "200" to "Low Speed",
                            "1000" to "Full Speed"
                        )
                    )
                ),
                onPinEventChanged = {_, _,_ ->},
                onHiddenButtonClicked = {_,_ ->},
                onAddEventClicked = {},
                onAddPinEvent = {},
                onDeletePinEvent = {}
            ) {

            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun EditEventPreviewDark(){
    CEBS_SCADATheme {
        Surface{
            EditEventScreen(
                pins = listOf(
                    PinEvent(
                        mcPinId = "1",
                        eventsMap = mapOf(
                            "true" to "Door Open",
                            "false" to "Door Closed"
                        )
                    ),
                    PinEvent(
                        mcPinId = "A1",
                        eventsMap = mapOf(
                            "200" to "Low Speed",
                            "1000" to "Full Speed"
                        )
                    )
                ),
                onPinEventChanged = {_, _,_ ->},
                onHiddenButtonClicked = {_,_ ->},
                onAddEventClicked = {},
                onAddPinEvent = {},
                onDeletePinEvent = {}
            ) {

            }
        }
    }
}