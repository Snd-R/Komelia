package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.ui.LoadState
import kotlinx.coroutines.flow.Flow

@Composable
expect fun ScreenPullToRefreshBox(
    screenState: Flow<LoadState<*>>,
    onRefresh: () -> Unit,
    minLoadDuration: Long = 700,
    content: @Composable BoxScope.() -> Unit,
)