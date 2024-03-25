package io.github.snd_r.komelia.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import io.github.snd_r.komelia.ui.common.images.BookThumbnail
import io.github.snd_r.komelia.ui.common.images.SeriesThumbnail
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesId

val expandedSearchBarWidth = 600.dp

@Composable
fun SearchBar(
    searchResults: SearchResults,
    query: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchAllClick: (String?) -> Unit,
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
                    onQueryChange("")
                    isFocused = false
                }

                is FocusInteraction.Focus -> isFocused = true
            }
        }
    }

    val focusManager = LocalFocusManager.current
    val isExpanded = derivedStateOf { isFocused && query.isNotBlank() }
    Box {

        SearchTextField(
            query = query,
            onQueryChange = onQueryChange,
            onSearchAllPress = onSearchAllClick,
            interactionSource = interactionSource
        )
        BoxWithConstraints {
            DropdownMenu(
                expanded = isExpanded.value,
                onDismissRequest = {},
                properties = PopupProperties(focusable = false),
                modifier = Modifier
                    .width(expandedSearchBarWidth)
                    .heightIn(max = this.maxHeight - 150.dp)
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
    onSearchAllClick: (String?) -> Unit,
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


    val series = searchResults.series
    if (series.isNotEmpty()) {
        Text(
            text = "SERIES",
            modifier = Modifier.padding(5.dp)
        )
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
            text = "BOOKS",
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

@Composable
private fun SeriesSearchEntry(
    series: KomgaSeries,
    library: KomgaLibrary?,
    onSeriesClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onSeriesClick() }
            .cursorForHand()
            .padding(10.dp)
    ) {
        SeriesThumbnail(series.id)
//        SeriesSimpleImageCard(series, null)
        Column(Modifier.padding(horizontal = 10.dp)) {
            Text(series.metadata.title)
            library?.let {
                Text("in ${library.name}")
            }
        }
    }
}

@Composable
private fun BookSearchEntry(
    book: KomgaBook,
    library: KomgaLibrary?,
    onBookClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onBookClick() }
            .padding(vertical = 10.dp)
    ) {
        BookThumbnail(book.id, Modifier.size(100.dp))
        Column() {
            Text(book.metadata.title)
            library?.let {
                Text("in ${library.name}")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchAllPress: (String?) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    modifier: Modifier = Modifier,
) {

    val colors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedTextColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.surfaceVariant
    )
    val isFocused = interactionSource.collectIsFocusedAsState()
    val textColor =
        if (isFocused.value) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onPrimaryContainer

    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor)
    val focusManager = LocalFocusManager.current

//    val animatedSize by animateDpAsState(
//        targetValue = if (isFocused.value) expandedSearchBarWidth else 300.dp,
//        animationSpec = tween(
//            durationMillis = 100,
//            easing = LinearEasing
//        )
//    )

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.then(
            Modifier
                .height(45.dp)
                .padding(top = 5.dp)
                .width(expandedSearchBarWidth)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                        focusManager.clearFocus()
                        onSearchAllPress(query)
                        return@onKeyEvent true
                    }

                    return@onKeyEvent false
                }

        ),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        textStyle = textStyle,
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = query,
            innerTextField = innerTextField,
            placeholder = { Text("Search", style = textStyle) },
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            trailingIcon = {
                Icon(
                    Icons.Filled.Search,
                    null,
                    modifier = Modifier
                        .clickable() { }
                        .cursorForHand()
                )
            },
            contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                top = 0.dp,
                bottom = 0.dp
            ),
            container = {
                OutlinedTextFieldDefaults.ContainerBox(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors,
                    shape = CircleShape
                )
            }
        )

    }
}
