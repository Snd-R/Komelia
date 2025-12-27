package snd.komelia.offline.readprogress.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.media.model.MediaExtensionEpub
import snd.komelia.offline.media.repository.OfflineMediaRepository
import snd.komelia.offline.readprogress.OfflineReadProgress
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.MediaProfile
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId

class ProgressMarkAction(
    private val mediaRepository: OfflineMediaRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {

    suspend fun run(
        bookId: KomgaBookId,
        userId: KomgaUserId,
        page: Int
    ) {
        transactionTemplate.execute {
            val media = mediaRepository.get(bookId)
            require(page in 1..media.pageCount) { "Page argument ($page) must be within 1 and book page count (${media.pageCount})" }

            val locator =
                if (media.mediaProfile == MediaProfile.EPUB) {
                    require(media.epubDivinaCompatible) { "epub book is not Divina compatible" }

                    val extension = media.extension
                    check(extension is MediaExtensionEpub)
                    extension.positions[page - 1]
                } else {
                    null
                }

            val progress = OfflineReadProgress(
                bookId = bookId,
                userId = userId,
                page = page,
                completed = page == media.pageCount,
                locator = locator
            )
            readProgressRepository.save(progress)
        }

        komgaEvents.emit(KomgaEvent.ReadProgressChanged(bookId, userId))
    }
}