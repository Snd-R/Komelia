package io.github.snd_r.komelia.ui.settings.updates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import io.github.snd_r.komelia.platform.DefaultDateTimeFormats.localDateFormat
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.dialogs.update.UpdateProgressDialog
import io.github.snd_r.komelia.updates.AppRelease
import io.github.snd_r.komelia.updates.AppVersion
import io.github.snd_r.komelia.updates.UpdateProgress
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
fun AppUpdatesContent(
    checkForUpdates: Boolean,
    onCheckForUpdatesChange: (Boolean) -> Unit,
    currentVersion: AppVersion,
    releases: List<AppRelease>,

    latestVersion: AppVersion?,
    lastChecked: Instant?,
    onCheckForUpdates: () -> Unit,
    versionCheckInProgress: Boolean,

    onUpdate: () -> Unit,
    onUpdateCancel: () -> Unit,
    downloadProgress: UpdateProgress?,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        SwitchWithLabel(
            checked = checkForUpdates,
            onCheckedChange = onCheckForUpdatesChange,
            label = { Text("Check for updates on startup") }
        )
        HorizontalDivider(Modifier.padding(bottom = 20.dp))
        VersionDetails(currentVersion, latestVersion, lastChecked, versionCheckInProgress)

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Bottom,
        ) {

            FilledTonalButton(
                onClick = { onCheckForUpdates() },
                shape = RoundedCornerShape(5.dp),
            ) { Text("Check for updates") }

            if (LocalPlatform.current != PlatformType.WEB_KOMF &&
                latestVersion != null && currentVersion < latestVersion
            ) {
                FilledTonalButton(
                    onClick = { onUpdate() },
                    shape = RoundedCornerShape(5.dp),
                ) { Text("Update") }
            }
        }

        if (releases.isNotEmpty()) {
            HorizontalDivider(Modifier.padding(vertical = 20.dp))
            Text("Release notes:", style = MaterialTheme.typography.headlineMedium)
            releases.forEach {
                ReleaseDetails(it)
                HorizontalDivider()
            }
        }

        if (downloadProgress != null) {
            UpdateProgressDialog(
                totalSize = downloadProgress.total,
                downloadedSize = downloadProgress.completed,
                onCancel = onUpdateCancel

            )
        }
    }
}

@Composable
private fun ReleaseDetails(release: AppRelease) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(release.version.toString(), style = MaterialTheme.typography.headlineMedium)
            val publishDate = remember {
                release.publishDate.toLocalDateTime(TimeZone.currentSystemDefault()).format(localDateFormat)
            }
            Text("release date: $publishDate", style = MaterialTheme.typography.labelLarge)
        }
        val state = rememberRichTextState()
        state.config.apply {
            linkColor = MaterialTheme.colorScheme.secondary
            linkTextDecoration = TextDecoration.Underline
            codeSpanBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
            codeSpanStrokeColor = MaterialTheme.colorScheme.surfaceVariant
        }
        remember { state.setMarkdown(release.releaseNotesBody) }
        RichText(state)
    }
}

@Composable
private fun VersionDetails(
    currentVersion: AppVersion,
    latestVersion: AppVersion?,
    lastChecked: Instant?,
    versionCheckInProgress: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Current version:", modifier = Modifier.widthIn(min = 200.dp))
            Text("$currentVersion")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (latestVersion != null) {
                Text("Latest checked version:", modifier = Modifier.widthIn(200.dp))
                Text("$latestVersion")

                if (lastChecked != null) {
                    lastChecked.toString()
                    val localDate = remember(lastChecked) {
                        lastChecked.toLocalDateTime(TimeZone.currentSystemDefault()).format(localDateFormat)
                    }
                    Text(
                        "checked at $localDate",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                if (versionCheckInProgress) {
                    CircularProgressIndicator()
                }
            }
        }

    }
}