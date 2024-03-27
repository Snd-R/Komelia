package io.github.snd_r.komelia.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.qos.logback.classic.spi.ILoggingEvent
import io.github.snd_r.komelia.platform.HorizontalScrollbar
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.ui.common.AppTheme
import kotlinx.coroutines.flow.SharedFlow
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timestampFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

@Composable
fun LogView(
    logsFlow: SharedFlow<ILoggingEvent>,
) {
    MaterialTheme(colorScheme = AppTheme.dark) {
        Surface(Modifier.fillMaxSize()) {
            LogsContent(logsFlow)
        }
    }
}

@Composable
private fun LogsContent(
    logsFlow: SharedFlow<ILoggingEvent>,
) {
    val scrollState = remember { LazyListState() }
    val logItems = remember(logsFlow) { mutableStateListOf<ILoggingEvent>() }

    LaunchedEffect(logsFlow) {
        logsFlow.collect { event ->
            val scrollToBottom = scrollState.layoutInfo.visibleItemsInfo
                .lastOrNull()?.index
                ?.let { lastIndex -> lastIndex >= logItems.size - 2 }
                ?: true

            logItems.add(event)
            if (logItems.size > 100) logItems.removeFirst()
            if (scrollToBottom) scrollState.scrollToItem(logItems.size)
        }
    }

    val horizontalScroll = rememberScrollState()
    Box(
        Modifier.padding(10.dp)
    ) {
        SelectionContainer {
            LazyColumn(
                state = scrollState,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp)
                    .fillMaxSize()
                    .horizontalScroll(horizontalScroll)
            ) {
                items(logItems) { LogMessage(it) }
                item { Spacer(Modifier.height(20.dp)) }
            }

        }
        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
        HorizontalScrollbar(horizontalScroll, Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun LogMessage(log: ILoggingEvent) {
    val timestamp = timestampFormatter.format(log.instant)
    val text = "$timestamp ${log.level} ${log.loggerName}".padEnd(70)
    val message = "$text  ${log.message}"

    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        lineHeight = 25.sp,
        fontFamily = FontFamily.Monospace,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth()
    )
}

