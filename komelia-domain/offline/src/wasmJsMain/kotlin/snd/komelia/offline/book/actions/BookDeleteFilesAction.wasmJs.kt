package snd.komelia.offline.book.actions

import io.github.vinceglb.filekit.PlatformFile
import snd.komelia.offline.action.OfflineAction

actual class BookDeleteFilesAction actual constructor() : OfflineAction {
    actual suspend fun execute(file: PlatformFile) {
    }
}