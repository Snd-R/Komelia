package snd.komelia.ui.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import snd.komelia.ui.LoadState

@Composable
actual fun ScreenPullToRefreshBox(
    screenState: Flow<LoadState<*>>,
    onRefresh: () -> Unit,
    minLoadDuration: Long,
    content: @Composable BoxScope.() -> Unit,
) = Box(content = content)
