package io.github.snd_r.komelia.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.NoPaddingTextField
import io.github.snd_r.komelia.ui.common.cards.BookSimpleImageCard
import io.github.snd_r.komelia.ui.common.cards.SeriesSimpleImageCard
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId

val expandedSearchBarWidth = 600.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchResults: SearchResults,
    query: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchAllClick: (String) -> Unit,
    libraryById: (KomgaLibraryId) -> KomgaLibrary?,
    onBookClick: (KomgaBookId) -> Unit,
    onSeriesClick: (KomgaSeriesId) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is FocusInteraction.Unfocus -> {
                    isFocused = false
                }

                is FocusInteraction.Focus -> isFocused = true
            }
        }
    }

    val focusManager = LocalFocusManager.current
    val isExpanded = derivedStateOf { isFocused && query.isNotBlank() }
    BoxWithConstraints(modifier) {
        val maxHeight = maxHeight
        val maxWidth = maxWidth
        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = isExpanded.value,
            onExpandedChange = {}
        ) {

            SearchTextField(
                query = query,
                onQueryChange = onQueryChange,
                onDone = onSearchAllClick,
                onDismiss = { onQueryChange("") },
                interactionSource = interactionSource,
                modifier = Modifier.menuAnchor()
            )
            DropdownMenu(
                expanded = isExpanded.value,
                onDismissRequest = {},
                properties = PopupProperties(focusable = false),
                modifier = Modifier
                    .width(maxWidth)
                    .heightIn(max = maxHeight - 150.dp)
                    .padding(5.dp)
            ) {
                SearchResultsDropDownBox(
                    currentQuery = query,
                    searchResults = searchResults,
                    isLoading = isLoading,
                    libraryById = libraryById,
                    onSearchAllClick = onSearchAllClick,
                    onSeriesClick = onSeriesClick,
                    onBookClick = onBookClick,
                    onDismiss = { focusManager.clearFocus() }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SearchResultsDropDownBox(
    currentQuery: String,
    searchResults: SearchResults,
    isLoading: Boolean,
    libraryById: (KomgaLibraryId) -> KomgaLibrary?,
    onSearchAllClick: (String) -> Unit,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    onBookClick: (KomgaBookId) -> Unit,
    onDismiss: () -> Unit,
) {
    if (currentQuery.isBlank()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable {
                onDismiss()
                onSearchAllClick(currentQuery)
            }
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text("Search all...")
    }
    if (isLoading) LinearProgressIndicator(
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.fillMaxWidth()
    )


    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val series = searchResults.series
        if (series.isNotEmpty()) {
            Text(text = "Series")
            series.forEach {
                SeriesSearchEntry(
                    series = it,
                    library = libraryById(it.libraryId),
                    onSeriesClick = {
                        onSeriesClick(it.id)
                        onDismiss()
                    }
                )
            }
        }
        val books = searchResults.books
        if (books.isNotEmpty()) {
            Text(
                text = "Books",
                modifier = Modifier.padding(5.dp)
            )
            books.forEach {
                BookSearchEntry(
                    book = it,
                    library = libraryById(it.libraryId),
                    onBookClick = {
                        onBookClick(it.id)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun EntryContainer(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() }
            .cursorForHand()
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content()
    }
}

@Composable
private fun SeriesSearchEntry(
    series: KomgaSeries,
    library: KomgaLibrary?,
    onSeriesClick: () -> Unit,
) {
    EntryContainer(onSeriesClick) {
        SeriesSimpleImageCard(
            series = series,
            onSeriesClick = onSeriesClick,
            modifier = Modifier
                .width(70.dp)
                .height(100.dp)
        )
        Column {
            Text(series.metadata.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
            library?.let { Text("in ${library.name}") }
        }
    }
}

@Composable
private fun BookSearchEntry(
    book: KomgaBook,
    library: KomgaLibrary?,
    onBookClick: () -> Unit,
) {
    EntryContainer(onBookClick) {
        BookSimpleImageCard(
            book = book,
            onBookClick = onBookClick,
            modifier = Modifier
                .width(70.dp)
                .height(100.dp)
        )
        Column {
            Text(book.metadata.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
            library?.let { Text("in ${library.name}") }
        }
    }
}


@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onDone: (String) -> Unit,
    onDismiss: () -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    NoPaddingTextField(
        text = query,
        placeholder = "Search",
        onTextChange = onQueryChange,
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedTextColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .height(45.dp)
            .fillMaxWidth()
            .padding(top = 5.dp)
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp -> {
                        focusManager.clearFocus()
                        onDone(query)
                        true
                    }

                    keyEvent.key == Key.Back || keyEvent.key == Key.Escape -> {
                        focusManager.clearFocus()
                        true
                    }

                    else -> false
                }
            },
        trailingIcon = {
            if (query.isNotBlank()) {
                Icon(
                    Icons.Filled.Close, null,
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                focusManager.clearFocus()
                                onDismiss()
                            }
                        ).cursorForHand(),
                    tint = MaterialTheme.colorScheme.secondary
                )
            } else {
                Icon(
                    Icons.Filled.Search, null,
                    modifier = Modifier.cursorForHand()
                )
            }
        },

        keyboardActions = KeyboardActions(
            onDone = { onDone(query) },
        )
    )
}
