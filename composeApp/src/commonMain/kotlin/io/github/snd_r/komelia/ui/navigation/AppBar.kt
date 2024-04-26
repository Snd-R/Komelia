package io.github.snd_r.komelia.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.snd_r.komelia.ui.search.SearchBar
import io.github.snd_r.komelia.ui.search.SearchResults
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.series.KomgaSeriesId
import kotlinx.coroutines.launch

@Composable
fun AppBar(
    onMenuButtonPress: suspend () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    onSearchAllClick: (String) -> Unit,
    searchResults: SearchResults,
    libraryById: (KomgaLibraryId) -> KomgaLibrary?,
    onBookClick: (KomgaBookId) -> Unit,
    onSeriesClick: (KomgaSeriesId) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        NavBarButton(onMenuButtonPress)

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            SearchBar(
                searchResults = searchResults,
                query = query,
                onQueryChange = onQueryChange,
                isLoading = isLoading,
                onSearchAllClick = onSearchAllClick,
                libraryById = libraryById,
                onBookClick = onBookClick,
                onSeriesClick = onSeriesClick
            )
        }
    }
}


@Composable
fun NavBarButton(
    onMenuButtonPress: suspend () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Box(
        contentAlignment = Alignment.CenterStart
    ) {
        IconButton(
            onClick = { coroutineScope.launch { onMenuButtonPress() } },
        ) {
            Icon(
                Icons.Rounded.Menu,
                contentDescription = null,
                modifier = Modifier
            )
        }
    }
}
