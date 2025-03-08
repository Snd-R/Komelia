package io.github.snd_r.komelia.ui.error

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.platform.HorizontalScrollbar
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.ui.common.AppTheme
import kotlinx.coroutines.launch


@Composable
fun ErrorView(
    exception: Throwable,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val stacktrace = exception.stackTraceToString().replace("\t", "    ")
    val errorText = remember {
        buildString {
            append("Encountered Unrecoverable Error: ")
            append("\"${exception::class.simpleName} ${exception.message}\"")
        }
    }
    ErrorView(
        exceptionMessage = errorText,
        stacktrace = stacktrace,
        isRestartable = exception !is NonRestartableException,
        onRestart = onRestart,
        onExit = onExit
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ErrorView(
    exceptionMessage: String,
    stacktrace: String?,
    isRestartable: Boolean,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    MaterialTheme(colorScheme = AppTheme.DARK.colorScheme) {
        val clipboardManager = LocalClipboardManager.current
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.safeDrawingPadding().padding(10.dp).fillMaxSize()
            ) {
                val scope = rememberCoroutineScope()
                val tooltipState = remember { TooltipState() }

                Text(exceptionMessage)
                if (stacktrace != null) {
                    StackTrace(stacktrace)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                ) {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { Text("Copied to clipboard") },
                        state = tooltipState,
                        enableUserInput = false
                    ) {
                        Button(
                            onClick = {
                                stacktrace?.let { clipboardManager.setText(AnnotatedString(stacktrace)) }
                                scope.launch { tooltipState.show() }
                            },
                        ) {
                            Text("Copy stacktrace to clipboard")
                        }
                    }
                    if (isRestartable) {
                        Button(
                            onClick = onRestart,
                        ) {
                            Text("Restart")
                        }

                    }
                    Button(
                        onClick = onExit,
                    ) {
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
        Modifier.padding(vertical = 10.dp)
            .weight(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
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