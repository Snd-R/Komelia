package snd.komelia.offline.readprogress

import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.R2Locator
import snd.komga.client.user.KomgaUserId
import kotlin.time.Clock
import kotlin.time.Instant

data class OfflineReadProgress(
    val bookId: KomgaBookId,
    val userId: KomgaUserId,
    val page: Int,
    val completed: Boolean,
    val readDate: Instant = Clock.System.now(),
    val deviceId: String = "",
    val deviceName: String = "",
    val locator: R2Locator? = null,
    val createdDate: Instant = Clock.System.now(),
    val lastModifiedDate: Instant = Clock.System.now(),
)
