package io.github.snd_r.komelia.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.onExternalDrag
import com.darkrockstudios.libraries.mpfilepicker.PlatformFile
import java.io.File
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun ExternalDragAndDropArea(
    onFileUpload: (List<PlatformFile>) -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.background(
            if (isDragging) MaterialTheme.colorScheme.secondary.copy(alpha = .5f)
            else MaterialTheme.colorScheme.surface
        ).onExternalDrag(
            onDragStart = { isDragging = true },
            onDragExit = { isDragging = false },
            onDrag = {},
            onDrop = { state ->
                val dragData = state.dragData
                if (dragData is DragData.FilesList) {
                    val files = dragData.readFiles().map { PlatformFile(File(URI(it))) }
                    onFileUpload(files)
                }
//                    if (dragData is DragData.Image)
                isDragging = false
            })
    ) {
        content()

    }

}
