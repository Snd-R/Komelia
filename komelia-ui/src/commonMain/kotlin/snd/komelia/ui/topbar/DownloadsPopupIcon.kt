package snd.komelia.ui.topbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.datetime.format
import snd.komelia.DefaultDateTimeFormats.localTimeFormat
import snd.komelia.formatDecimal
import snd.komelia.ui.platform.VerticalScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsPopupIcon(
    state: NotificationsState,
    modifier: Modifier
) {
    val notifications = state.notifications.collectAsState(emptyList()).value
    if (notifications.isEmpty()) return


    val unreadNotifications = state.unreadNotifications.collectAsState().value
    val showNotifications = state.notificationsOpen.collectAsState().value
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(width = 40.dp, height = 40.dp)
            .clip(IconButtonDefaults.standardShape)
            .clickable(onClick = {
                state.onNotificationsOpen()
            })
    ) {
        BadgedBox(
            badge = {
                if (unreadNotifications > 0) {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text("$unreadNotifications")
                    }
                }
            },
        ) {
            Icon(
                Icons.Default.Download, null,
            )

            if (showNotifications) {
                Popup(
                    popupPositionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Below,
                        1.dp
                    ),
                    onDismissRequest = { state.onNotificationsClose() },
                ) {
                    NotificationsContent(
                        notifications = notifications,
                        onNotificationsClear = state::onNotificationsClear
                    )
                }
            }
        }

    }
}

@Composable
private fun NotificationsContent(
    notifications: Collection<Notification>,
    onNotificationsClear: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Surface(
        modifier = Modifier.widthIn(max = 400.dp).heightIn(min = 200.dp, max = 600.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent downloads", modifier = Modifier.padding(start = 5.dp))
                Spacer(Modifier.weight(1f))
                ElevatedButton(onClick = onNotificationsClear) {
                    Text("Clear all")
                    Icon(Icons.Default.Clear, null)
                }
            }
            HorizontalDivider()

            Box(
                Modifier.background(MaterialTheme.colorScheme.surfaceVariant)

            ) {
                Column(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .verticalScroll(scrollState)
                ) {
                    for (notification in notifications) {
                        when (notification) {
                            is BookNotification -> BookNotification(notification)
                        }
                        HorizontalDivider()
                    }
                }
                VerticalScrollbar(scrollState, modifier = Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

@Composable
private fun BookNotification(notification: BookNotification) {
    Column(
        modifier = Modifier.padding(3.dp).fillMaxWidth(),
    ) {
        Text(notification.book.metadata.title, style = MaterialTheme.typography.bodyMedium)

        when (notification) {
            is BookNotification.BookDownloadProgress -> BookNotificationProgress(notification)
            is BookNotification.BookDownloadCompleted -> BookNotificationCompleted(notification)
        }
    }
}

@Composable
private fun BookNotificationProgress(event: BookNotification.BookDownloadProgress) {
    if (event.totalProgress == 0L)
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    else {
        LinearProgressIndicator(
            progress = { event.completedProgress / event.totalProgress.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )

        val totalMiB = remember(event.totalProgress) {
            (event.totalProgress.toFloat() / 1024 / 1024).formatDecimal(2)
        }
        val completedMiB = remember(event.completedProgress) {
            (event.completedProgress.toFloat() / 1024 / 1024).formatDecimal(2)
        }
        Text("${completedMiB}MiB / ${totalMiB}MiB", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BookNotificationCompleted(event: BookNotification.BookDownloadCompleted) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(event.book.size, style = MaterialTheme.typography.bodyMedium)
        Text(event.timestamp.format(localTimeFormat), style = MaterialTheme.typography.bodyMedium)
    }
}
