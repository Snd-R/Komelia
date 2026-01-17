package snd.komelia.offline.readprogress.actions

import io.ktor.http.*
import kotlinx.coroutines.flow.MutableSharedFlow
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.media.model.MediaExtensionEpub
import snd.komelia.offline.media.repository.OfflineMediaRepository
import snd.komelia.offline.readprogress.OfflineReadProgress
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.MediaProfile
import snd.komga.client.book.R2Progression
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId
import kotlin.math.roundToInt

class ProgressMarkProgressionAction(
    private val mediaRepository: OfflineMediaRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {

    suspend fun run(
        bookId: KomgaBookId,
        userId: KomgaUserId,
        newProgression: R2Progression,
    ) {
        transactionTemplate.execute {
            val media = mediaRepository.get(bookId)
            val mediaProfile = checkNotNull(media.mediaProfile) { "Media has not profile" }

            val progress = when (mediaProfile) {
                MediaProfile.DIVINA,
                MediaProfile.PDF -> {
                    val newLocations = requireNotNull(newProgression.locator.locations)
                    check(newLocations.position in 1..media.pageCount) { "Page argument (${newLocations.position}) must be within 1 and book page count (${media.pageCount})" }

                    OfflineReadProgress(
                        bookId = bookId,
                        userId = userId,
                        page = newProgression.locator.locations!!.position!!,
                        completed = newLocations.position == media.pageCount,
                        readDate = newProgression.modified,
                        deviceId = newProgression.device.id,
                        deviceName = newProgression.device.name,
                        locator = newProgression.locator,
                    )

                }

                MediaProfile.EPUB -> {
                    val href =
                        newProgression.locator.href
                            .replaceAfter("#", "")
                            .removeSuffix("#")
                            .decodeURLPart()
                    requireNotNull(newProgression.locator.locations?.progression) { "location.progression is required" }


                    val extension = media.extension
                    check(extension is MediaExtensionEpub) { "Epub extension not found" }
                    // match progression with positions
                    val matchingPositions = extension.positions.filter { it.href == href }
                    val matchedPosition =
                        if (extension.isFixedLayout && matchingPositions.size == 1)
                            matchingPositions.first()
                        else
                            matchingPositions.firstOrNull { it.locations!!.progression == newProgression.locator.locations!!.progression }
                                ?: run {
                                    // no exact match
                                    val before =
                                        matchingPositions.filter { it.locations!!.progression!! < newProgression.locator.locations!!.progression!! }
                                            .maxByOrNull { it.locations!!.position!! }
                                    val after =
                                        matchingPositions.filter { it.locations!!.progression!! > newProgression.locator.locations!!.progression!! }
                                            .minByOrNull { it.locations!!.position!! }
                                    if (before == null || after == null || before.locations!!.position!! > after.locations!!.position!!)
                                        throw IllegalArgumentException("Invalid progression")
                                    before
                                }

                    val totalProgression = matchedPosition.locations?.totalProgression
                    OfflineReadProgress(
                        bookId = bookId,
                        userId = userId,
                        page = totalProgression?.let { (media.pageCount * it).roundToInt() } ?: 0,
                        completed = totalProgression?.let { it >= 0.99F } ?: false,
                        readDate = newProgression.modified,
                        deviceId = newProgression.device.id,
                        deviceName = newProgression.device.name,
                        locator = newProgression.locator.copy(
                            // use the type we have instead of the one provided
                            type = matchedPosition.type,
                            // if no koboSpan is provided, use the one we matched
                            koboSpan = newProgression.locator.koboSpan ?: matchedPosition.koboSpan,
                            // don't trust the provided total progression, the one from Kobo can be wrong
                            locations = newProgression.locator.locations?.copy(totalProgression = totalProgression),
                        ),
                    )
                }
            }

            readProgressRepository.save(progress)
        }

        komgaEvents.emit(KomgaEvent.ReadProgressChanged(bookId, userId))
    }
}