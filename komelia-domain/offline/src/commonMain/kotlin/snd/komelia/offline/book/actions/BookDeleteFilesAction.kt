package snd.komelia.offline.book.actions

import io.github.vinceglb.filekit.PlatformFile
import snd.komelia.offline.action.OfflineAction

expect class BookDeleteFilesAction() : OfflineAction {
    suspend fun execute(file: PlatformFile)
}
