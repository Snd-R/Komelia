package io.github.snd_r.komelia.ui.settings.authactivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.ui.common.Pagination
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toLocalDateTime
import snd.komga.client.user.KomgaAuthenticationActivity

@Composable
fun AuthenticationActivityContent(
    activity: List<KomgaAuthenticationActivity>,
    forMe: Boolean,
    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        activity.forEach {
            AuthenticationInfoCard(
                activity = it,
                showEmail = !forMe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )
        }

        Pagination(
            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

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
                    activity.dateTime.toLocalDateTime(currentSystemDefault()).toString()
                }
                Text(formattedDate)
                Spacer(Modifier.weight(1f))
                Text("Source: ${activity.source}")
            }

            val email = activity.email
            if (showEmail && email != null) {
                Text(
                    email,
                    color = MaterialTheme.colorScheme.secondary,
                )

                Spacer(Modifier.width(20.dp))
            }

            Row {
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
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                } else {
                    Text(activity.error ?: "")

                    Icon(
                        Icons.Default.Error,
                        null,
                        tint = MaterialTheme.colorScheme.error,
                    )

                }
            }

            HorizontalDivider(Modifier.padding(vertical = 5.dp))
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
