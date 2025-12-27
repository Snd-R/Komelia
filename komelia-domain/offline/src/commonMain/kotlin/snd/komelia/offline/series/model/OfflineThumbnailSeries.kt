package snd.komelia.offline.series.model

import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesThumbnail

data class OfflineThumbnailSeries(
    val id: KomgaThumbnailId,
    val seriesId: KomgaSeriesId,
    val type: Type,
    val selected: Boolean,
    val mediaType: String,
    val fileSize: Long,
    val width: Int,
    val height: Int,
    val url: String?,
    val thumbnail: ByteArray?
) {
    fun doesNotExist(): Boolean {
        return thumbnail == null
    }

    enum class Type {
        SIDECAR,
        USER_UPLOADED,
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as OfflineThumbnailSeries

        if (selected != other.selected) return false
        if (fileSize != other.fileSize) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (id != other.id) return false
        if (seriesId != other.seriesId) return false
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
        result = 31 * result + seriesId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }
}

fun KomgaSeriesThumbnail.toOfflineThumbnailSeries(thumbnailBytes: ByteArray) =
    OfflineThumbnailSeries(
        id = this.id,
        seriesId = this.seriesId,
        type = OfflineThumbnailSeries.Type.valueOf(
            this.type
        ),
        selected = this.selected,
        mediaType = this.mediaType,
        fileSize = this.fileSize,
        width = this.width,
        height = this.height,
        url = null,
        thumbnail = thumbnailBytes
    )