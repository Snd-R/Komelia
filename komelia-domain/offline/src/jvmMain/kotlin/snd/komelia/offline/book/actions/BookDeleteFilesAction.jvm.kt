package snd.komelia.offline.book.actions

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import snd.komelia.offline.action.OfflineAction

actual class BookDeleteFilesAction : OfflineAction {
    actual suspend fun execute(file: PlatformFile) {
        val filePath = Path(file.file.path)
        SystemFileSystem.delete(filePath, mustExist = false)

        val parent = filePath.parent
        if (parent != null && SystemFileSystem.exists(parent) && SystemFileSystem.list(parent).isEmpty()) {
            SystemFileSystem.delete(parent)
        }
    }
}