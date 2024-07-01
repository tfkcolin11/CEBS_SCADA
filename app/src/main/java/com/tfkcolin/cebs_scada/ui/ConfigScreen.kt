package com.tfkcolin.cebs_scada.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tfkcolin.cebs_scada.ui.editevent.HeaderConfig
import com.tfkcolin.cebs_scada.util.KeyMapPreferences
import kotlinx.coroutines.launch

@Composable
fun ConfigScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit,
    state: SnackbarHostState,
    isAdmin: Boolean,
    onIsAdminChanged: (Boolean) -> Unit
){
    val context = LocalContext.current
    val preferences = remember {
        KeyMapPreferences(context = context)
    }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    AnimatedVisibility(
        visible = isAdmin,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LazyColumn(
            modifier = modifier
        ){
            item {
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.titleMedium
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                TextButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    onClick = { onNavigate("edit_home") }
                ) {
                    Icon(
                        modifier = Modifier.padding(end = 15.dp),
                        imageVector = Icons.Filled.Home,
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Edit home screen",
                        textAlign = TextAlign.Start
                    )
                }
                TextButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    onClick = {
                        onNavigate("edit_key")
                    }
                ) {
                    Icon(
                        modifier = Modifier.padding(end = 15.dp),
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Edit keys",
                        textAlign = TextAlign.Start
                    )
                }
                TextButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    onClick = {
                        onNavigate("edit_event")
                    }
                ) {
                    Icon(
                        modifier = Modifier.padding(end = 15.dp),
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Configure events",
                        textAlign = TextAlign.Start
                    )
                }
            }
            item {
                var passWordText by remember { mutableStateOf("") }
                var confirmPassWordText by remember { mutableStateOf("") }
                Text(
                    modifier = Modifier.padding(top = 10.dp),
                    text = "Password",
                    style = MaterialTheme.typography.titleSmall
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                HeaderConfig(
                    text = passWordText,
                    onTextChanged = {passWordText = it},
                    confirmText = confirmPassWordText,
                    onConFirmTextChanged = {confirmPassWordText = it},
                    isConfirmable = true,
                    autoFocus = false,
                    isSecret = true,
                    buttonColor = MaterialTheme.colorScheme.secondary,
                    addButtonText = "Change",
                    labelText = "New",
                    confirmTextLabel = "Confirm",
                    createText = "Ok",
                    placeholderText = "jevaisalecole",
                    color = Color.Unspecified,
                    onCreateClicked = {
                        if(passWordText == confirmPassWordText) {
                            preferences.setPwd(passWordText)
                            scope.launch {
                                state.showSnackbar("password has been changed")
                            }
                        }
                    }
                )
            }
        }
    }
    AnimatedVisibility(
        visible = !isAdmin,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Card(modifier = Modifier.fillMaxWidth(.7f)) {
                var pwd by remember { mutableStateOf("")}
                LazyVerticalGrid(
                    modifier = Modifier.padding(10.dp),
                    columns = GridCells.Fixed(2)
                ){
                    item {
                        Text(text = "Password")
                    }
                    item {
                        TextField(
                            value = pwd,
                            onValueChange = {pwd = it},
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focus.clearFocus()
                                }
                            )
                        )
                    }
                    item{
                        Spacer(modifier = Modifier.fillMaxHeight())
                    }
                    item{
                        TextButton(
                            onClick = {
                                if(pwd == preferences.pwd())
                                    onIsAdminChanged(true)
                                else
                                    scope.launch {
                                        state.showSnackbar("Wrong password")
                                    }
                            }
                        ) {
                            Text(text = "Ok")
                        }
                    }
                }
            }
        }
    }
}