package com.tfkcolin.cebs_scada.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.tfkcolin.cebs_scada.R

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
){
    val uriHandler = LocalUriHandler.current
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Developed by Cameroon Eco-building Solution Sarl",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Image(
                modifier = Modifier
                    .size(150.dp),
                painter = painterResource(id = R.drawable.sarlbig),
                contentDescription = "drawer image"
            )
            val annotatedString = buildAnnotatedString {
                append("* Project manager\n")
                withStyle(
                    SpanStyle(color = Color.Yellow)
                ){
                    append("    BAKAM Edith Bleck\n\n")
                }
                append("* Chef programmer\n")
                withStyle(
                    SpanStyle(color = Color.Yellow)
                ){
                    append("    Tambo Fotsing Kevin Colin\n\n")
                }
                append("* Team\n")
                withStyle(
                    SpanStyle(color = Color.Yellow)
                ){
                    append("    - Dinya Yannick\n")
                    append("    - Tankwa Maxime\n\n")
                }
                length
                append("\n\nContact: \n" +
                        "Developer : ")
                val size = length
                val text = "tfkcolin11@gmail.com"
                withStyle(
                    style = SpanStyle(
                        color = Color.Cyan,
                        textDecoration = TextDecoration.Underline
                    )
                ){
                    append("tfkcolin11@gmail.com")
                }
                addStringAnnotation(
                    tag = "URL",
                    annotation = "mailto:tfkcolin11@gmail.com",
                    start = size,
                    end = size + text.length
                )
            }
            Text(
                modifier = Modifier
                    .pointerInput(Unit){
                        detectTapGestures(
                            onTap = { offsetPosition->
                                layoutResult?.let {
                                    val pos = it.getOffsetForPosition(offsetPosition)
                                    annotatedString.getStringAnnotations(pos, pos).firstOrNull()?.let { res ->
                                        if(res.tag == "URL")
                                            uriHandler.openUri(res.item)
                                    }
                                }
                            }
                        )
                    }
                    .padding(top = 30.dp),
                text = annotatedString,
                onTextLayout = {layoutResult = it}
            )
        }
    }
}