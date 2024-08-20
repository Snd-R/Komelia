package io.github.snd_r.komelia.platform

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.PlatformFiles
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
actual fun ExternalDragAndDropArea(
    onFileUpload: (PlatformFiles) -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    val dragAndDropTarget = remember {
        object : DragAndDropTarget {

            override fun onStarted(event: DragAndDropEvent) {
                isDragging = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDragging = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val dragData = event.awtTransferable
                if (dragData.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    val files = (dragData.getTransferData(DataFlavor.javaFileListFlavor) as List<*>)
                        .filterIsInstance<File>().map { PlatformFile(it) }
                    onFileUpload(files)
                    return true
                }
                return true
            }
        }
    }
    Column(
        modifier = modifier.background(
            if (isDragging) MaterialTheme.colorScheme.secondary.copy(alpha = .5f)
            else MaterialTheme.colorScheme.surface
        ).dragAndDropTarget(
            shouldStartDragAndDrop = { true },
            target = dragAndDropTarget
        )
    ) {
        content()

    }

}
