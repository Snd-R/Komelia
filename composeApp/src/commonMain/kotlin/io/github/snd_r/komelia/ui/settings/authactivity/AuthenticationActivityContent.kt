package io.github.snd_r.komelia.ui.settings.authactivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.ui.platform.VerticalScrollbar
import io.github.snd_r.komga.user.KomgaAuthenticationActivity
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun AuthenticationActivityContent(
    activity: List<KomgaAuthenticationActivity>,
    forMe: Boolean,
    loadMoreEntries: () -> Unit,
) {
    val listState = rememberLazyListState()
    val reachedBottom by remember { derivedStateOf { listState.reachedBottom() } }
    LaunchedEffect(reachedBottom) {
        if (reachedBottom) loadMoreEntries()
    }

    Row {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            item {
                Text(
                    "Authentication Activity",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            items(activity) {
                AuthenticationInfoCard(
                    activity = it,
                    showEmail = !forMe,
                    modifier = Modifier.width(500.dp).height(130.dp)
                )

            }
        }

        VerticalScrollbar(listState, Modifier.align(Alignment.Top))
    }
}

internal fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}

@Composable
private fun AuthenticationInfoCard(
    activity: KomgaAuthenticationActivity,
    showEmail: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(modifier) {
        Column(Modifier.padding(10.dp)) {
            Row {
                val formattedDate = remember(activity) {
                    activity.dateTime
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm", Locale.ENGLISH))
                }
                Text(formattedDate)
                Spacer(Modifier.weight(1f))
                Text("Source: ${activity.source}")
            }

            Row {

                val email = activity.email
                if (showEmail && email != null) {
                    Text(
                        email,
                        color = MaterialTheme.colorScheme.secondary,
                    )

                    Spacer(Modifier.width(20.dp))
                }

                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("IP: ")
                        }
                        append(activity.ip)
                    },
                )
                Spacer(Modifier.weight(1f))
                if (activity.success) {
                    Text("Successful")
                    Icon(
                        Icons.Default.Done,
                        null,
                        tint = Color.Green,
                    )
                } else {
                    Text(activity.error ?: "")

                    Icon(
                        Icons.Default.Error,
                        null,
                        tint = Color.Red,
                    )

                }
            }

            Divider(Modifier.padding(vertical = 5.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("User Agent: ") }
                    withStyle(SpanStyle(fontSize = 14.sp)) {
                        append(activity.userAgent)
                    }
                }
            )
        }
    }
}
