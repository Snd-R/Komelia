package io.github.snd_r.komelia.ui.navigation

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.platform.WindowSizeClass.FULL
import io.github.snd_r.komelia.ui.LocalWindowState
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.search.SearchBar
import io.github.snd_r.komelia.ui.search.SearchResults
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries

@Composable
fun AppBar(
    onMenuButtonPress: suspend () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    onSearchAllClick: (String) -> Unit,
    searchResults: SearchResults,
    libraryById: (KomgaLibraryId) -> KomgaLibrary?,
    onBookClick: (KomgaBook) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
) {
    PlatformTitleBar {
        val coroutineScope = rememberCoroutineScope()

        IconButton(
            modifier = Modifier.align(Alignment.Start),
            onClick = { coroutineScope.launch { onMenuButtonPress() } },
        ) {
            Icon(Icons.Rounded.Menu, null)
        }

        val searchBarModifier = when (LocalWindowWidth.current) {
            FULL -> Modifier.align(Alignment.CenterHorizontally).width(600.dp)
            else -> Modifier.align(Alignment.Start).width(300.dp)
        }

        SearchBar(
            modifier = searchBarModifier,
            searchResults = searchResults,
            query = query,
            onQueryChange = onQueryChange,
            isLoading = isLoading,
            onSearchAllClick = onSearchAllClick,
            libraryById = libraryById,
            onBookClick = onBookClick,
            onSeriesClick = onSeriesClick
        )


        val windowState = LocalWindowState.current
        val isFullscreen = windowState.isFullscreen.collectAsState(false)
        if (isFullscreen.value) {
            IconButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { coroutineScope.launch { windowState.setFullscreen(false) } },
            ) {
                Icon(Icons.Default.FullscreenExit, null)
            }
        }
    }
}
