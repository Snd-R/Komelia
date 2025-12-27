package snd.komelia.offline.book.actions

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.parent
import snd.komelia.offline.action.OfflineAction

actual class BookDeleteFilesAction : OfflineAction {
    actual suspend fun execute(file: PlatformFile) {
        val parent = file.parent()
        file.delete(false)
        if (parent != null && parent.exists() && parent.list().isEmpty()) {
            parent.delete(false)
        }
    }
}