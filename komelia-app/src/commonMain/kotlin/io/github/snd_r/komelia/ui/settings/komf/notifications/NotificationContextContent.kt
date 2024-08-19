package io.github.snd_r.komelia.ui.settings.komf.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.NumberField
import io.github.snd_r.komelia.ui.settings.komf.notifications.KomfNotificationSettingsViewModel.NotificationContextState
import io.github.snd_r.komelia.ui.settings.komf.notifications.KomfNotificationSettingsViewModel.NotificationContextState.AlternativeTitleContext
import io.github.snd_r.komelia.ui.settings.komf.notifications.KomfNotificationSettingsViewModel.NotificationContextState.AuthorContext
import io.github.snd_r.komelia.ui.settings.komf.notifications.KomfNotificationSettingsViewModel.NotificationContextState.BookContextState
import io.github.snd_r.komelia.ui.settings.komf.notifications.KomfNotificationSettingsViewModel.NotificationContextState.WebLinkContext

@Composable
fun NotificationContextDialogContent(
    state: NotificationContextState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Library", style = MaterialTheme.typography.titleLarge)
        TextField(
            value = state.libraryId,
            onValueChange = state::libraryId::set,
            label = { Text("Id \$library.id") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.libraryName,
            onValueChange = state::libraryName::set,
            label = { Text("Name \$library.name") },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()

        Text("Series", style = MaterialTheme.typography.titleLarge)
        TextField(
            value = state.seriesId,
            onValueChange = state::seriesId::set,
            label = { Text("Id \$series.id") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesName,
            onValueChange = state::seriesName::set,
            label = { Text("Name \$series.name") },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.seriesBookCount,
            onValueChange = { state.seriesBookCount = it },
            label = { Text("Book Count \$series.bookCount") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesStatus,
            onValueChange = state::seriesStatus::set,
            label = { Text("Status \$series.metadata.status") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesTitle,
            onValueChange = state::seriesTitle::set,
            label = { Text("Metadata Title \$series.metadata.title") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesTitleSort,
            onValueChange = state::seriesTitleSort::set,
            label = { Text("Metadata Title Sort \$series.metadata.titleSort") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesSummary,
            onValueChange = state::seriesSummary::set,
            label = { Text("Summary \$series.metadata.summary") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesReadingDirection,
            onValueChange = state::seriesReadingDirection::set,
            label = { Text("Reading Direction \$series.metadata.readingDirection") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesPublisher,
            onValueChange = state::seriesPublisher::set,
            label = { Text("Publisher \$series.metadata.publisher") },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.seriesAgeRating,
            onValueChange = state::seriesAgeRating::set,
            label = { Text("Age Rating \$series.metadata.ageRating") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.seriesLanguage,
            onValueChange = state::seriesLanguage::set,
            label = { Text("Language \$series.metadata.language") },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.seriesTotalBookCount,
            onValueChange = state::seriesTotalBookCount::set,
            label = { Text("Total Book Count \$series.metadata.totalBookCount") },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.seriesReleaseYer,
            onValueChange = state::seriesReleaseYer::set,
            label = { Text("Release Year \$series.metadata.releaseYear") },
            modifier = Modifier.fillMaxWidth()
        )
        StringValueList(state.seriesGenres, state::seriesGenres::set, "Genres \$series.metadata.genres[i]")
        StringValueList(state.seriesTags, state::seriesTags::set, "Tags \$series.metadata.tags[i]")
        StringValueList(
            state.seriesAlternativePublishers,
            state::seriesAlternativePublishers::set,
            "Alternative Publishers \$series.metadata.alternativePublishers[i]"
        )
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceDim).padding(start = 10.dp)) {
            ValueList(
                values = state.seriesAlternativeTitles,
                valueName = "Aluuternative Title",
                onAdd = state::onSeriesAlternativeTitleAdd,
                onDelete = state::onSeriesAlternativeTitleDelete,
                content = { AlternativeTitlesEdit(it) }
            )
            HorizontalDivider()
            ValueList(
                values = state.seriesAuthors,
                valueName = "Author",
                onAdd = state::onSeriesAuthorAdd,
                onDelete = state::onSeriesAuthorDelete,
                content = { AuthorsEdit(it) }
            )
            HorizontalDivider()
            ValueList(
                values = state.seriesLinks,
                valueName = "Link",
                onAdd = state::onSeriesLinkAdd,
                onDelete = state::onSeriesLinkDelete,
                content = { WebLinksEdit(it) }
            )
        }

        HorizontalDivider()
        Text("Books", style = MaterialTheme.typography.titleLarge)
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
                    Text("Book ${index + 1}")
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

        FilledTonalButton(onClick = state::onBookAdd, shape = RoundedCornerShape(5.dp)) { Text("Add Book") }
    }
}


@Composable
private fun BookContext(state: BookContextState) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        TextField(
            value = state.id,
            onValueChange = state::id::set,
            label = { Text("Id \$books[i].id") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.name,
            onValueChange = state::name::set,
            label = { Text("Name \$books[i].name") },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.number,
            onValueChange = { state.number = it ?: 0 },
            label = { Text("Number \$books[i].number") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.title,
            onValueChange = state::title::set,
            label = { Text("Metadata Title \$books[i].metadata.title") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.summary,
            onValueChange = state::summary::set,
            label = { Text("Summary \$books[i].metadata.summary") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.metadataNumber,
            onValueChange = state::metadataNumber::set,
            label = { Text("Metadata Number \$books[i].metadata.number") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.metadataNumberSort,
            onValueChange = state::metadataNumberSort::set,
            label = { Text("Metadata Number Sort \$books[i].metadata.numberSort") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.releaseDate,
            onValueChange = state::releaseDate::set,
            label = { Text("Release Date \$books[i].metadata.releaseDate") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.isbn,
            onValueChange = state::isbn::set,
            label = { Text("ISBN \$books[i].metadata.isbn") },
            modifier = Modifier.fillMaxWidth()
        )

        StringValueList(state.tags, state::tags::set, "Tags \$book[i].metadata.tags[i]")
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceDim).padding(start = 10.dp)) {
            ValueList(
                values = state.authors,
                valueName = "Author",
                onAdd = state::onAuthorAdd,
                onDelete = state::onAuthorDelete,
                content = { AuthorsEdit(it) }
            )
            HorizontalDivider()
            ValueList(
                values = state.links,
                valueName = "Link",
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
                    Text("$valueName ${index + 1}")
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
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.cursorForHand()
        ) { Text("Add $valueName") }
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
            label = { Text("Name \$series.metadata.authors[i].name") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.role,
            onValueChange = state::role::set,
            label = { Text("Role \$series.metadata.authors[i].role") },
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
            label = { Text("Label \$series.metadata.links[i].label") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.url,
            onValueChange = state::url::set,
            label = { Text("Url \$series.metadata.links[i].url") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
