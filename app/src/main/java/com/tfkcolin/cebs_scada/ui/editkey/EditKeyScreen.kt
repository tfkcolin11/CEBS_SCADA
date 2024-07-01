package com.tfkcolin.cebs_scada.ui.editkey

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tfkcolin.cebs_scada.ui.editevent.HeaderConfig
import com.tfkcolin.cebs_scada.data.MapKey
import com.tfkcolin.cebs_scada.ui.theme.CEBS_SCADATheme

@Composable
fun EditKeyScreen(
    modifier: Modifier = Modifier,
    keys: List<MapKey>,
    state: LazyListState = rememberLazyListState(),
    temps: List<MapKey> = listOf(),
    onHiddenButtonClicked: (MapKey) -> Unit,
    onDataChanged: (MapKey) -> Unit,
    onAddKey: (MapKey) -> Unit,
    onClearTemp: () -> Unit
){
    DisposableEffect(Unit){
        onDispose {
            onClearTemp()
        }
    }
    KeyMapConfigLayout(
        modifier = modifier,
        state = state,
        temps = temps,
        keys = keys,
        onKeyChanged = {
            onDataChanged(it)
        },
        header = {
            var text by remember { mutableStateOf("") }
            HeaderConfig(
                text = text,
                addButtonText = "Add key",
                labelText = "Key label",
                onTextChanged = { text = it },
                placeholderText = "Close",
                onCreateClicked = {
                    onAddKey(
                        MapKey(
                            key = text
                        )
                    )
                }
            )
        },
        onHiddenButtonClicked = onHiddenButtonClicked
    )
}

@Preview
@Composable
fun EditKeyPreview(){
    CEBS_SCADATheme {
        Surface {
            EditKeyScreen(
                keys = listOf(
                    MapKey(
                        key = "Go",
                        cmd = "900566"
                    ),
                    MapKey(
                        key = "Close",
                        cmd = "900566"
                    ),
                    MapKey(
                        key = "Open",
                        cmd = "900566"
                    ),
                ),
                onHiddenButtonClicked = {},
                onDataChanged = {},
                onAddKey = {}
            ) {

            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun EditKeyPreviewDark(){
    CEBS_SCADATheme {
        Surface {
            EditKeyScreen(
                keys = listOf(
                    MapKey(
                        key = "Go",
                        cmd = "900566"
                    ),
                    MapKey(
                        key = "Close",
                        cmd = "900566"
                    ),
                    MapKey(
                        key = "Open",
                        cmd = "900566"
                    ),
                ),
                onHiddenButtonClicked = {},
                onDataChanged = {},
                onAddKey = {}
            ) {

            }
        }
    }
}