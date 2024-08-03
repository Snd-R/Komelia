package io.github.snd_r.komelia.platform

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.core.PlatformFiles

@Composable
actual fun ExternalDragAndDropArea(
    onFileUpload: (PlatformFiles) -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        content()
    }
}