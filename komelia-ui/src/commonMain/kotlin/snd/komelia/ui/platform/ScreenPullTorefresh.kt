package snd.komelia.ui.platform

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import snd.komelia.ui.LoadState

@Composable
expect fun ScreenPullToRefreshBox(
    screenState: Flow<LoadState<*>>,
    onRefresh: () -> Unit,
    minLoadDuration: Long = 700,
    content: @Composable BoxScope.() -> Unit,
)