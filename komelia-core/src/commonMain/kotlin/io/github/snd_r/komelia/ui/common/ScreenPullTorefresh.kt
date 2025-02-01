package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.ui.LoadState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.takeWhile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPullToRefreshBox(
    screenState: Flow<LoadState<*>>,
    onRefresh
    : () -> Unit,
    minLoadDuration: Long = 700,
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