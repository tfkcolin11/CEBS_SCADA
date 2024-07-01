package com.tfkcolin.cebs_scada.ui.editkey

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfkcolin.cebs_scada.data.MapKey
import com.tfkcolin.cebs_scada.ui.theme.CEBS_SCADATheme

@Composable
fun MapItem(
    modifier: Modifier = Modifier,
    keyText: String,
    onKeyTextChanged: (String) -> Unit,
    cmdText: String,
    onCmdTextChanged: (String) -> Unit,
    keyLabelText: String = "Key",
    cmdLabelText: String = "Command",
    keyIsSingleLine: Boolean = true,
    cmdIsSingleLine: Boolean = true,
    isKeyEditable: Boolean = true,
    isCmdEditable: Boolean = true,
    onHiddenButtonClicked: () -> Unit
){
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var maxReveal by remember { mutableIntStateOf(20) }
    var height by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier.height(with(LocalDensity.current) { height.toDp() }),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = .8f)
        ) {
            IconButton(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .onSizeChanged { maxReveal = it.width },
                onClick = onHiddenButtonClicked
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete"
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { height = it.height },
            shape = RectangleShape
        ) {
            Column(
                modifier = Modifier
                    .padding(5.dp)
            ) {
                Row(modifier = Modifier
                    .padding(bottom = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        modifier = Modifier.width(100.dp),
                        text = "$keyLabelText: "
                    )
                    if(isKeyEditable) {
                        TextField(
                            value = keyText,
                            onValueChange = onKeyTextChanged,
                            singleLine = keyIsSingleLine,
                            readOnly = !isKeyEditable,
                            trailingIcon = {
                                IconButton(onClick = {
                                    onKeyTextChanged("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Edit key",
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = .5f)
                                    )

                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                            )
                        )
                    }
                    else{
                        Text(
                            modifier = Modifier
                                .padding(5.dp),
                            text = keyText,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = .8f)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        modifier = Modifier.width(100.dp),
                        text = "$cmdLabelText: "
                    )
                    if(isCmdEditable) {
                        TextField(
                            value = cmdText,
                            onValueChange = onCmdTextChanged,
                            readOnly = !isCmdEditable,
                            singleLine = cmdIsSingleLine,
                            trailingIcon = {
                                IconButton(onClick = {
                                    onCmdTextChanged("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Edit key",
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = .5f)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            )
                        )
                    }
                    else{
                        Text(
                            modifier = Modifier
                                .padding(5.dp),
                            text = cmdText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeyMapConfigLayout(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    keys: List<MapKey>,
    temps: List<MapKey> = listOf(),
    onKeyChanged: (MapKey) -> Unit,
    header: @Composable () -> Unit = {
        Surface(
            color = MaterialTheme
                .colorScheme.primary.copy(alpha = .8f)
        ) {
            Text(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                text = "Map your buttons here",
                style = MaterialTheme.typography.titleSmall
            )
        }
    },
    onHiddenButtonClicked: (MapKey) -> Unit
){
    Column(modifier = modifier
        .fillMaxWidth()) {
        header()
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 10.dp),
            state = state
        ){
            items(keys){ key ->
                var codeText by remember { mutableStateOf(key.cmd) }
                var keyText by remember { mutableStateOf(key.key) }
                LaunchedEffect(temps){
                    if(key.id in temps.map { it.id }) {
                        val elt = temps.indexOfFirst { it.id == key.id }
                        codeText = temps[elt].cmd
                        keyText = temps[elt].key
                    }
                }
                MapItem(
                    keyText = keyText,
                    onKeyTextChanged = {
                        keyText = it
                        onKeyChanged(
                            MapKey(
                                id = key.id,
                                key = it,
                                cmd = codeText
                            )
                        )
                    },
                    cmdText = codeText,
                    onCmdTextChanged = {
                        codeText = it
                        onKeyChanged(
                            MapKey(
                                id = key.id,
                                key = keyText,
                                cmd = it
                            )
                        )
                    },
                    isKeyEditable = true,
                    onHiddenButtonClicked = {
                        onHiddenButtonClicked(key)
                    }
                )
            }
        }
    }
}

@Composable
fun HomeFloatingActionButton(
    modifier: Modifier = Modifier,
    extended: Boolean,
    onClick: () -> Unit,
    text: @Composable () -> Unit = {
        Text(
            text = "Add note",
            modifier = Modifier
                .padding(start = 8.dp, top = 3.dp)
        )
    },
    icon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = null
        )
    }
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateContentSize()
        ) {
            icon()
            AnimatedVisibility (extended) {
                text()
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
fun ItemPreview(){
    CEBS_SCADATheme {
        Surface {
            KeyMapConfigLayout(
                keys = listOf(
                    MapKey(
                        key = "A",
                        cmd = "202333"
                    ),
                    MapKey(
                        key = "B",
                        cmd = "202353"
                    ),
                    MapKey(
                        key = "C",
                        cmd = "201353"
                    )
                ),
                onKeyChanged = {},
                onHiddenButtonClicked = {}
            )
        }
    }
}