package io.github.snd_r.komelia.ui.error

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.common.CustomTheme
import io.github.snd_r.komelia.platform.HorizontalScrollbar
import io.github.snd_r.komelia.platform.VerticalScrollbar
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorView(
    exception: Throwable,
    windowHeight: Dp, windowWidth: Dp,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    CustomTheme(windowHeight, windowWidth) {
        val clipboardManager = LocalClipboardManager.current

        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(10.dp).fillMaxSize()
            ) {
                val stacktrace = exception.stackTraceToString().replace("\t", "    ")
                val scope = rememberCoroutineScope()
                val tooltipState = remember { TooltipState() }
                Text("Encountered Unrecoverable Error: \"${exception.message}\"")
                StackTrace(stacktrace)
                Row {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { Text("Copied to clipboard") },
                        state = tooltipState
                    ) {
                        Button(onClick = {
                            clipboardManager.setText(AnnotatedString(stacktrace))
                            scope.launch { tooltipState.show() }
                        }) {
                            Text("Copy stacktrace to clipboard")
                        }
                    }
                    Spacer(Modifier.size(50.dp))
                    Button(onClick = onRestart) {
                        Text("Restart")
                    }
                    Spacer(Modifier.size(50.dp))
                    Button(onClick = onExit) {
                        Text("Exit")
                    }
                }
            }
        }
    }
}


@Composable
private fun ColumnScope.StackTrace(
    stacktrace: String,
) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        Modifier.padding(horizontal = 50.dp, vertical = 10.dp)
            .weight(1f)
            .background(AppTheme.colors.material.surfaceVariant)
            .clickable { clipboardManager.setText(AnnotatedString(stacktrace)) }

    ) {
        val verticalScroll = rememberScrollState()
        val horizontalScroll = rememberScrollState()
        Box {
            Column(
                Modifier.padding(10.dp)
                    .verticalScroll(verticalScroll)
                    .horizontalScroll(horizontalScroll)
            ) {
                Text(
                    text = stacktrace,
                    style = TextStyle(fontSize = 14.sp)
                )
            }

            HorizontalScrollbar(horizontalScroll, Modifier.align(Alignment.BottomEnd))
            VerticalScrollbar(verticalScroll, Modifier.align(Alignment.TopEnd))
        }
    }

}