package snd.komelia.offline.book.model

import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookThumbnail
import snd.komga.client.common.KomgaThumbnailId

data class OfflineThumbnailBook(
    val id: KomgaThumbnailId,
    val bookId: KomgaBookId,
    val type: Type,
    val selected: Boolean,
    val mediaType: String,
    val fileSize: Long,
    val width: Int,
    val height: Int,
    val url: String?,
    val thumbnail: ByteArray?
) {

    enum class Type {
        GENERATED,
        SIDECAR,
        USER_UPLOADED,
    }

    fun doesNotExist(): Boolean {
//        if (url != null) return Files.exists(Paths.get(url.toURI()))
        return thumbnail == null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as OfflineThumbnailBook

        if (selected != other.selected) return false
        if (fileSize != other.fileSize) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (id != other.id) return false
        if (bookId != other.bookId) return false
        if (type != other.type) return false
        if (mediaType != other.mediaType) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selected.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + id.hashCode()
        result = 31 * result + bookId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }
}

fun KomgaBookThumbnail.toOfflineThumbnailBook(thumbnailBytes: ByteArray) =
    OfflineThumbnailBook(
        id = this.id,
        bookId = this.bookId,
        type = runCatching {
            OfflineThumbnailBook.Type.valueOf(
                this.type
            )
        }.getOrDefault(OfflineThumbnailBook.Type.GENERATED),
        selected = this.selected,
        mediaType = this.mediaType,
        fileSize = this.fileSize,
        width = this.width,
        height = this.height,
        url = null,
        thumbnail = thumbnailBytes
    )
