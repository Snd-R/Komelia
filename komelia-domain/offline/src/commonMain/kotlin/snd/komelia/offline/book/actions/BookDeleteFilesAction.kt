package snd.komelia.offline.book.actions

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import snd.komelia.offline.action.OfflineAction

expect class BookDeleteFilesAction(downloadsDirectory: Flow<PlatformFile>) : OfflineAction {
    suspend fun execute(file: PlatformFile)
}
