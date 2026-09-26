package snd.komelia.ui.settings.komf.mangabaka

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_database_download_date
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database_checksum
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database_download
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database_update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import snd.komelia.DefaultDateTimeFormats.localDateFormat
import snd.komelia.ui.common.components.SwitchWithLabel
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.settings.komf.providers.DatabaseDownloadContent
import snd.komf.api.config.DownloadProgress
import snd.komf.api.config.MangaBakaDatabaseDto

@Composable
fun KomfMangaBakaSettingsContent(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    mangaBakaDbMetadata: MangaBakaDatabaseDto?,
    onMangaBakaUpdate: () -> Flow<DownloadProgress>,
    error: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        SwitchWithLabel(
            checked = enabled,
            onCheckedChange = onEnabledChange,
            label = { Text("Display MangaBaka metadata") },
            supportingText = { Text("Requires downloaded database and linked MangaBaka series") }
        )

        var showMangaBakaDownloadProgress by remember { mutableStateOf(false) }
        if (showMangaBakaDownloadProgress) {
            DatabaseDownloadContent(
                onMangaBakaUpdate,
                { showMangaBakaDownloadProgress = false })
        }
        HorizontalDivider()

        if (error != null) {
            Text("Error", style = MaterialTheme.typography.titleLarge)
            Text(error)
            return
        }

        Text(
            stringResource(Res.string.komf_providers_mangabaka_database),
            style = MaterialTheme.typography.titleLarge
        )
        Column {
            if (mangaBakaDbMetadata != null) {
                val downloadDate = remember(mangaBakaDbMetadata) {
                    mangaBakaDbMetadata.downloadTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(localDateFormat)
                }
                Text(stringResource(Res.string.komf_providers_database_download_date, downloadDate))
                Text(
                    stringResource(
                        Res.string.komf_providers_mangabaka_database_checksum, mangaBakaDbMetadata.checksum.trim()
                    )
                )
            }
            FilledTonalButton(
                onClick = { showMangaBakaDownloadProgress = true },
                modifier = Modifier.cursorForHand()
            ) {
                Text(
                    if (mangaBakaDbMetadata != null) stringResource(Res.string.komf_providers_mangabaka_database_update)
                    else stringResource(Res.string.komf_providers_mangabaka_database_download)
                )
            }
        }


    }
}