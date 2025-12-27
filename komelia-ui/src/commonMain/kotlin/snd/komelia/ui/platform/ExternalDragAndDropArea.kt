package snd.komelia.ui.platform

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
expect fun ExternalDragAndDropArea(
    onFileUpload: (List<PlatformFile>) -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
)