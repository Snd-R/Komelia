package snd.komelia.ui.settings.komf.notifications.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.m3.ChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_authors_name
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_authors_role
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_add
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_author
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_id
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_isbn
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_link
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_metadata_number
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_metadata_number_sort
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_metadata_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_name
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_number
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_release_date
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_summary
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_book_tags
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_close
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_library
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_library_id
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_library_name
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_list_value_add
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_list_value_and_number
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_preview
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_age_rating
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_alternative_publishers
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_alternative_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_author
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_book_count
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_book_number
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_books
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_genres
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_id
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_language
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_link
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_metadata_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_metadata_title_sort
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_name
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_publisher
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_reading_direction
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_release_year
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_status
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_summary
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_tags
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_series_total_book_count
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_weblink_label
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_context_weblink_url
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.common.components.NumberField
import snd.komelia.ui.dialogs.AppDialog
import snd.komelia.ui.dialogs.DialogSimpleHeader
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.settings.komf.notifications.NotificationContextState
import snd.komelia.ui.settings.komf.notifications.NotificationContextState.AlternativeTitleContext
import snd.komelia.ui.settings.komf.notifications.NotificationContextState.AuthorContext
import snd.komelia.ui.settings.komf.notifications.NotificationContextState.BookContextState
import snd.komelia.ui.settings.komf.notifications.NotificationContextState.WebLinkContext


@Composable
fun NotificationContextDialog(
    notificationContextState: NotificationContextState,
    onDismissRequest: () -> Unit,
) {
    AppDialog(
        modifier = Modifier.widthIn(max = 800.dp),
        header = { DialogSimpleHeader(stringResource(Res.string.komf_notification_context_preview)) },
        content = { NotificationContextDialogContent(notificationContextState) },
        controlButtons = {
            FilledTonalButton(
                onClick = onDismissRequest,
            ) {
                Text(stringResource(Res.string.komf_notification_context_close))
            }
        },
        onDismissRequest = onDismissRequest,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
fun NotificationContextDialogContent(
    state: NotificationContextState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(
            stringResource(Res.string.komf_notification_context_library),
            style = MaterialTheme.typography.titleLarge
        )
        TextField(
            value = state.libraryId,
            onValueChange = state::libraryId::set,
            label = { Text(stringResource(Res.string.komf_notification_context_library_id)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.libraryName,
            onValueChange = state::libraryName::set,
            label = { Text(stringResource(Res.string.komf_notification_context_library_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()

        Text(
            stringResource(Res.string.komf_notification_context_series),
            style = MaterialTheme.typography.titleLarge
        )
        TextField(
            value = state.seriesId,
            onValueChange = state::seriesId::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_id)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesName,
            onValueChange = state::seriesName::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.seriesBookCount,
            onValueChange = { state.seriesBookCount = it },
            label = { Text(stringResource(Res.string.komf_notification_context_series_book_count)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesStatus,
            onValueChange = state::seriesStatus::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_status)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesTitle,
            onValueChange = state::seriesTitle::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_metadata_title)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesTitleSort,
            onValueChange = state::seriesTitleSort::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_metadata_title_sort)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesSummary,
            onValueChange = state::seriesSummary::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_summary)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesReadingDirection,
            onValueChange = state::seriesReadingDirection::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_reading_direction)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesPublisher,
            onValueChange = state::seriesPublisher::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_publisher)) },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.seriesAgeRating,
            onValueChange = state::seriesAgeRating::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_age_rating)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesLanguage,
            onValueChange = state::seriesLanguage::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_language)) },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.seriesTotalBookCount,
            onValueChange = state::seriesTotalBookCount::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_total_book_count)) },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.seriesReleaseYer,
            onValueChange = state::seriesReleaseYer::set,
            label = { Text(stringResource(Res.string.komf_notification_context_series_release_year)) },
            modifier = Modifier.fillMaxWidth()
        )
        StringValueList(
            state.seriesGenres,
            state::seriesGenres::set,
            stringResource(Res.string.komf_notification_context_series_genres)
        )
        StringValueList(
            state.seriesTags,
            state::seriesTags::set,
            stringResource(Res.string.komf_notification_context_series_tags)
        )
        StringValueList(
            state.seriesAlternativePublishers,
            state::seriesAlternativePublishers::set,
            stringResource(Res.string.komf_notification_context_series_alternative_publishers)
        )
        Column(Modifier.padding(start = 10.dp)) {
            ValueList(
                values = state.seriesAlternativeTitles,
                valueName = stringResource(Res.string.komf_notification_context_series_alternative_title),
                onAdd = state::onSeriesAlternativeTitleAdd,
                onDelete = state::onSeriesAlternativeTitleDelete,
                content = { AlternativeTitlesEdit(it) }
            )
            HorizontalDivider()
            ValueList(
                values = state.seriesAuthors,
                valueName = stringResource(Res.string.komf_notification_context_series_author),
                onAdd = state::onSeriesAuthorAdd,
                onDelete = state::onSeriesAuthorDelete,
                content = { AuthorsEdit(it) }
            )
            HorizontalDivider()
            ValueList(
                values = state.seriesLinks,
                valueName = stringResource(Res.string.komf_notification_context_series_link),
                onAdd = state::onSeriesLinkAdd,
                onDelete = state::onSeriesLinkDelete,
                content = { WebLinksEdit(it) }
            )
        }

        HorizontalDivider()
        Text(
            stringResource(Res.string.komf_notification_context_series_books),
            style = MaterialTheme.typography.titleLarge
        )
        state.books.forEachIndexed { index, book ->
            var showBook by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showBook = !showBook }.cursorForHand()

                ) {
                    Icon(if (showBook) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    Text(stringResource(Res.string.komf_notification_context_series_book_number, index + 1))
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { state.onBookDelete(book) }) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
                AnimatedVisibility(
                    visible = showBook,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    BookContext(book)
                }
                HorizontalDivider()
            }

        }

        FilledTonalButton(onClick = state::onBookAdd) { Text(stringResource(Res.string.komf_notification_context_book_add)) }
    }
}


@Composable
private fun BookContext(state: BookContextState) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        TextField(
            value = state.id,
            onValueChange = state::id::set,
            label = { Text(stringResource(Res.string.komf_notification_context_book_id)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.name,
            onValueChange = state::name::set,
            label = { Text(stringResource(Res.string.komf_notification_context_book_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.number,
            onValueChange = { state.number = it ?: 0 },
            label = { Text(stringResource(Res.string.komf_notification_context_book_number)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.title,
            onValueChange = state::title::set,
            label = { Text(stringResource(Res.string.komf_notification_context_book_metadata_title)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.summary,
            onValueChange = state::summary::set,
            label = { Text(stringResource(Res.string.komf_notification_context_book_summary)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.metadataNumber,
            onValueChange = state::metadataNumber::set,
            label = { Text(stringResource(Res.string.komf_notification_context_book_metadata_number)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.metadataNumberSort,
            onValueChange = state::metadataNumberSort::set,
            label = { Text(stringResource(Res.string.komf_notification_context_book_metadata_number_sort)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.releaseDate,
            onValueChange = state::releaseDate::set,
            label = { Text(stringResource(Res.string.komf_notification_context_book_release_date)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.isbn,
            onValueChange = state::isbn::set,
            label = { Text(stringResource(Res.string.komf_notification_context_book_isbn)) },
            modifier = Modifier.fillMaxWidth()
        )

        StringValueList(
            state.tags,
            state::tags::set,
            stringResource(Res.string.komf_notification_context_book_tags),
        )
        Column(Modifier.padding(start = 10.dp)) {
            ValueList(
                values = state.authors,
                valueName = stringResource(Res.string.komf_notification_context_book_author),
                onAdd = state::onAuthorAdd,
                onDelete = state::onAuthorDelete,
                content = { AuthorsEdit(it) }
            )
            HorizontalDivider()
            ValueList(
                values = state.links,
                valueName = stringResource(Res.string.komf_notification_context_book_link),
                onAdd = state::onLinkAdd,
                onDelete = state::onLinkDelete,
                content = { WebLinksEdit(it) }
            )
        }
    }
}


@Composable
private fun StringValueList(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    label: String,
) {
    val valuesState = rememberChipTextFieldState(values.map { Chip(it) })
    LaunchedEffect(values) {
        snapshotFlow { valuesState.chips.map { it.text } }.collect { onValuesChange(it) }
    }
    ChipTextField(
        state = valuesState,
        label = { Text(label) },
        onSubmit = { text -> Chip(text) },
        readOnlyChips = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun <T> ValueList(
    values: List<T>,
    valueName: String,
    onAdd: () -> Unit,
    onDelete: (T) -> Unit,
    content: @Composable (T) -> Unit,
) {
    Column {
        values.forEachIndexed { index, value ->
            var showBook by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showBook = !showBook }.cursorForHand()

                ) {
                    Icon(if (showBook) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    Text(
                        stringResource(
                            Res.string.komf_notification_context_list_value_and_number, valueName, index + 1
                        )
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onDelete(value) }) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
                AnimatedVisibility(
                    visible = showBook,
                ) {
                    content(value)
                }
            }

        }
        FilledTonalButton(
            onClick = onAdd,
            modifier = Modifier.cursorForHand()
        ) { Text(stringResource(Res.string.komf_notification_context_list_value_add, valueName)) }
    }
}

@Composable
private fun AlternativeTitlesEdit(state: AlternativeTitleContext) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        TextField(
            value = state.label,
            onValueChange = state::label::set,
            label = { Text("Label \$series.metadata.alternativeTitles[i].label") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.title,
            onValueChange = state::title::set,
            label = { Text("Title \$series.metadata.alternativeTitles[i].title") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AuthorsEdit(state: AuthorContext) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        TextField(
            value = state.name,
            onValueChange = state::name::set,
            label = { Text(stringResource(Res.string.komf_notification_context_authors_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.role,
            onValueChange = state::role::set,
            label = { Text(stringResource(Res.string.komf_notification_context_authors_role)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun WebLinksEdit(state: WebLinkContext) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        TextField(
            value = state.label,
            onValueChange = state::label::set,
            label = { Text(stringResource(Res.string.komf_notification_context_weblink_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.url,
            onValueChange = state::url::set,
            label = { Text(stringResource(Res.string.komf_notification_context_weblink_url)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
