package snd.komelia.ui.platform

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.takeWhile
import snd.komelia.ui.LoadState

@Composable
actual fun ScreenPullToRefreshBox(
    screenState: Flow<LoadState<*>>,
    onRefresh
    : () -> Unit,
    minLoadDuration: Long,
    content: @Composable BoxScope.() -> Unit,
) {
    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) return@LaunchedEffect
        delay(minLoadDuration)
        screenState.takeWhile { it == LoadState.Loading }.collect {}
        isRefreshing = false
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            onRefresh()
            isRefreshing = true
        }
    ) {
        content()
    }
}
