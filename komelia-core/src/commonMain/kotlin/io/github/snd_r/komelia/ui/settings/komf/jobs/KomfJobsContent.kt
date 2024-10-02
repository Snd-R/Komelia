package io.github.snd_r.komelia.ui.settings.komf.jobs

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.DefaultDateTimeFormats.dateTimeFormat
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.AppFilterChipDefaults
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.cards.SeriesImageCard
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.format
import snd.komf.api.job.KomfMetadataJob
import snd.komf.api.job.KomfMetadataJobStatus
import snd.komf.api.job.KomfMetadataJobStatus.COMPLETED
import snd.komf.api.job.KomfMetadataJobStatus.FAILED
import snd.komf.api.job.KomfMetadataJobStatus.RUNNING
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun KomfJobsContent(
    jobs: List<KomfMetadataJob>,
    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    selectedStatus: KomfMetadataJobStatus?,
    onStatusSelect: (KomfMetadataJobStatus?) -> Unit,
    getSeries: suspend (KomgaSeriesId) -> KomgaSeries?,
    onSeriesClick: (KomgaSeries) -> Unit,
    onDeleteAll: () -> Unit,
    isLoading: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            StatusFilters(
                selectedStatus = selectedStatus,
                onStatusSelect = onStatusSelect,
                onDeleteAll = onDeleteAll
            )
            HorizontalDivider()
        }
        Pagination(
            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Crossfade(
            targetState = isLoading,
            animationSpec = tween(500),
        ) { loading ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (loading) CircularProgressIndicator()
                else if (jobs.isEmpty()) {
                    Text("Nothing to show")
                } else {
                    key(currentPage, selectedStatus) {
                        jobs.forEach {
                            JobCard(
                                job = it,
                                getSeries = getSeries,
                                onSeriesClick = onSeriesClick
                            )
                        }
                    }
                }
            }
        }
        if (jobs.size > 10) {
            Pagination(
                totalPages = totalPages,
                currentPage = currentPage,
                onPageChange = onPageChange,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Spacer(Modifier.height(30.dp))

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobCard(
    job: KomfMetadataJob,
    getSeries: suspend (KomgaSeriesId) -> KomgaSeries?,
    onSeriesClick: (KomgaSeries) -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var series by remember { mutableStateOf<KomgaSeries?>(null) }
    var seriesTitle by remember { mutableStateOf("") }
    LaunchedEffect(job) {
        val seriesId = launch {
            delay(100)
            seriesTitle = job.seriesId.value
        }
        loading = true
        series = getSeries(KomgaSeriesId(job.seriesId.value))
        seriesId.cancel()
        seriesTitle = series?.metadata?.title ?: job.seriesId.value
        loading = false
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .height(70.dp)
            .padding(end = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            state = rememberTooltipState(isPersistent = true),
            tooltip = { SeriesTooltip(series, loading) },
            modifier = Modifier
                .clickable { series?.let { onSeriesClick(it) } }
                .padding(10.dp)
                .cursorForHand()
                .width(200.dp)
        ) {
            Crossfade(
                targetState = seriesTitle,
                animationSpec = tween(500),
            ) { title ->
                Text(
                    text = title,
                    textDecoration = TextDecoration.Underline,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )
            }
        }
        job.finishedAt?.let {
            val duration: Duration = it.minus(job.startedAt)
            Text("duration: ${duration.toString(DurationUnit.SECONDS, 2)}")
        }

        Spacer(Modifier.weight(1f))
        Text(job.startedAt.format(dateTimeFormat))
        when (job.status) {
            RUNNING -> {}
            FAILED ->
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    state = rememberTooltipState(isPersistent = true),
                    tooltip = {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .9f),
                            border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.surface),
                            modifier = Modifier.widthIn(max = 400.dp),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            job.message?.let {
                                Text(it, modifier = Modifier.padding(10.dp))
                            }

                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }

            COMPLETED -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun StatusFilters(
    selectedStatus: KomfMetadataJobStatus?,
    onStatusSelect: (KomfMetadataJobStatus?) -> Unit,
    onDeleteAll: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = selectedStatus == null,
            onClick = { onStatusSelect(null) },
            label = { Text("All") },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null
        )

        FilterChip(
            selected = selectedStatus == RUNNING,
            onClick = { onStatusSelect(RUNNING) },
            label = { Text("Running") },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null
        )
        FilterChip(
            selected = selectedStatus == COMPLETED,
            onClick = { onStatusSelect(COMPLETED) },
            label = { Text("Completed") },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null
        )
        FilterChip(
            selected = selectedStatus == FAILED,
            onClick = { onStatusSelect(FAILED) },
            label = { Text("Failed") },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null
        )
        Spacer(Modifier.weight(1f))
        var showConfirmationDialog by remember { mutableStateOf(false) }
        FilledTonalButton(
            onClick = { showConfirmationDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.cursorForHand()
        ) {
            Text("Delete all")
        }
        if (showConfirmationDialog) {
            ConfirmationDialog(
                body = "Delete job history?",
                buttonConfirmColor = MaterialTheme.colorScheme.errorContainer,
                onDialogConfirm = onDeleteAll,
                onDialogDismiss = { showConfirmationDialog = false }
            )
        }
    }
}

@Composable
private fun SeriesTooltip(
    series: KomgaSeries?,
    loading: Boolean
) {
    if (series == null)
        Card(
            Modifier
                .width(150.dp)
                .aspectRatio(0.703f)
                .padding(15.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            if (loading) CircularProgressIndicator()
            else {
                Spacer(Modifier.weight(1f))
                Text("Unknown series")
            }
        }
    else SeriesImageCard(
        series = series,
        onSeriesClick = {},
        modifier = Modifier.width(150.dp)
    )

}