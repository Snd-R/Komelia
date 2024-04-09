package io.github.snd_r.komelia.platform

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.darkrockstudios.libraries.mpfilepicker.PlatformFile

@Composable
expect fun ExternalDragAndDropArea(
    onFileUpload: (List<PlatformFile>) -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
)