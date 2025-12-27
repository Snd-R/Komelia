package snd.komelia.offline.media.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.book.MediaProfile
import snd.komga.client.book.R2Locator
import snd.komga.client.book.WPPublication

data class OfflineMedia(
    val bookId: KomgaBookId,
    val status: KomgaMediaStatus,
    val mediaType: String?,
    val mediaProfile: MediaProfile?,
    val comment: String,
    val epubDivinaCompatible: Boolean,
    val epubIsKepub: Boolean = false,
    val pageCount: Int,
    val pages: List<OfflineBookPage>,

    // epub extensions
    val extension: MediaExtension? = null,
)

@Serializable
sealed interface MediaExtension

@Serializable
@SerialName("MediaExtensionEpub")
data class MediaExtensionEpub(
    val toc: List<EpubTocEntry> = emptyList(),
    val landmarks: List<EpubTocEntry> = emptyList(),
    val pageList: List<EpubTocEntry> = emptyList(),
    val isFixedLayout: Boolean = false,
    val positions: List<R2Locator> = emptyList(),

    // cached from komga response
    val manifest: WPPublication
) : MediaExtension
