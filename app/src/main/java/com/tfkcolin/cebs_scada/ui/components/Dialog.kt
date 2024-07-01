package com.tfkcolin.cebs_scada.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tfkcolin.cebs_scada.R

@Composable
fun ExplanatoryPermissionDialog(
    modifier: Modifier = Modifier,
    explanatoryPages: List<@Composable () -> Unit>,
    onIgnore: () -> Unit
){
    val (index, onIndexChanged) = remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Card(
            modifier = modifier
                .animateContentSize()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                        .sizeIn(maxHeight = 50.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        modifier = Modifier
                            .padding(end = 15.dp),
                        painter = painterResource(id = R.drawable.sarlbig), contentDescription = null)
                    Text(
                        text = "Important !!",
                        textAlign = TextAlign.Center,
                        color = Color.Red,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                val out = remember {
                    listOf(
                        {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                text = "this application uses features that need your approval to work",
                                textAlign = TextAlign.Center,
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        if(index + 1 < explanatoryPages.size)
                                            onIndexChanged(index + 1)
                                    }
                                ) {
                                    Text(text = "Learn more")
                                }
                                TextButton(onClick = onIgnore) {
                                    Text(text = "Ignore")
                                }
                            }
                        },
                        *explanatoryPages.toTypedArray()
                    )
                }
                out[index]()
                if(index != 0){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ){
                        AnimatedVisibility(visible = index > 0) {
                            TextButton(
                                onClick = { onIndexChanged(index - 1) }
                            ) {
                                Text(text = "Prev")
                            }
                        }
                        AnimatedVisibility(visible = index < out.size - 1) {
                            TextButton(onClick = { onIndexChanged(index + 1) }) {
                                Text(text = "Next")
                            }
                        }
                    }
                }
            }
        }
    }
}