package snd.komelia.offline.book.actions

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import snd.komelia.offline.action.OfflineAction

actual class BookDeleteFilesAction actual constructor(downloadsDirectory: Flow<PlatformFile>) : OfflineAction {
    actual suspend fun execute(file: PlatformFile) {
    }
}