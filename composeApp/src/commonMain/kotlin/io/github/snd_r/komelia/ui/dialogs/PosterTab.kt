package io.github.snd_r.komelia.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.MultipleFilePicker
import io.github.snd_r.komelia.image.ImageTypeDetector
import io.github.snd_r.komelia.platform.ExternalDragAndDropArea
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.cards.ThumbnailEditCard
import io.github.snd_r.komelia.ui.common.cards.ThumbnailUploadCard
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.ThumbnailToBeUploaded
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komga.book.KomgaBookThumbnail
import io.github.snd_r.komga.common.KomgaThumbnailId
import io.github.snd_r.komga.series.KomgaSeriesThumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.math.roundToInt

class PosterTab(private val state: PosterEditState) : DialogTab {

    override fun options() = TabItem(
        title = "POSTER",
        icon = Icons.Default.Image,
    )

    @Composable
    override fun Content() {
        PosterEditContent(posterState = state)
    }
}

class PosterEditState {
    val thumbnails = mutableStateListOf<KomgaThumbnail>()
    val userUploadedThumbnails = mutableStateListOf<ThumbnailToBeUploaded>()

    suspend fun onThumbnailUpload(paths: List<Path>) {
        withContext(Dispatchers.IO) {
            val toUpload = paths
                .filter { ImageTypeDetector.isSupportedImageType(it) }
                .map {
                    ThumbnailToBeUploaded(
                        selected = true,
                        size = it.fileSize(),
                        path = it,
                    )
                }

            if (toUpload.isNotEmpty()) {
                thumbnails.replaceAll { it.copy(selected = false) }
                userUploadedThumbnails.replaceAll { it.copy(selected = false) }
                userUploadedThumbnails.addAll(toUpload.dropLast(1))
                userUploadedThumbnails.add(toUpload.last().copy(selected = true))
            }
        }
    }

    fun onExistingThumbnailSelect(thumbnail: KomgaThumbnail) {
        thumbnails.replaceAll {
            if (it.id == thumbnail.id) it.copy(selected = true, deleted = false)
            else it.copy(selected = false)
        }
        userUploadedThumbnails.replaceAll { it.copy(selected = false) }
    }

    fun onExistingThumbnailDelete(thumb: KomgaThumbnail) {
        thumbnails.replaceAll {
            if (it.id == thumb.id) it.copy(deleted = !it.markedDeleted, selected = false)
            else it
        }
    }

    fun onUploadThumbnailDelete(file: Path) {
        userUploadedThumbnails.removeAll { it.path == file }
    }

    fun onUploadThumbnailSelect(file: Path) {
        thumbnails.replaceAll { it.copy(selected = false) }
        userUploadedThumbnails.replaceAll {
            if (it.path == file) it.copy(selected = true)
            else it
        }
    }

    sealed interface KomgaThumbnail {
        val id: KomgaThumbnailId
        val type: ThumbnailType
        val mediaType: String
        val fileSize: Long
        val width: Int
        val height: Int
        val selected: Boolean

        val markedSelected: Boolean
        val markedDeleted: Boolean

        fun copy(selected: Boolean = this.markedSelected, deleted: Boolean = this.markedDeleted): KomgaThumbnail

        class SeriesThumbnail(
            val komgaThumbnail: KomgaSeriesThumbnail,
            override val markedSelected: Boolean = komgaThumbnail.selected,
            override val markedDeleted: Boolean = false,
        ) : KomgaThumbnail {
            override val id: KomgaThumbnailId = komgaThumbnail.id
            override val type: ThumbnailType = ThumbnailType.from(komgaThumbnail.type)
            override val mediaType: String = komgaThumbnail.mediaType
            override val fileSize: Long = komgaThumbnail.fileSize
            override val width: Int = komgaThumbnail.width
            override val height: Int = komgaThumbnail.height
            override val selected: Boolean = komgaThumbnail.selected

            override fun copy(selected: Boolean, deleted: Boolean): KomgaThumbnail {
                return SeriesThumbnail(komgaThumbnail, selected, deleted)
            }
        }

        class BookThumbnail(
            val komgaThumbnail: KomgaBookThumbnail,
            override val markedSelected: Boolean = komgaThumbnail.selected,
            override val markedDeleted: Boolean = false,
        ) : KomgaThumbnail {
            override val id: KomgaThumbnailId = komgaThumbnail.id
            override val type: ThumbnailType = ThumbnailType.from(komgaThumbnail.type)
            override val mediaType: String = komgaThumbnail.mediaType
            override val fileSize: Long = komgaThumbnail.fileSize
            override val width: Int = komgaThumbnail.width
            override val height: Int = komgaThumbnail.height
            override val selected: Boolean = komgaThumbnail.selected

            override fun copy(selected: Boolean, deleted: Boolean): KomgaThumbnail {
                return BookThumbnail(komgaThumbnail, selected, deleted)
            }
        }

        data class ThumbnailToBeUploaded(
            val selected: Boolean = true,
            val size: Long,
            val path: Path,
        )
    }

    enum class ThumbnailType {
        USER_UPLOADED,
        SIDECAR,
        GENERATED,
        UNKNOWN;

        companion object {
            fun from(value: String) = when (value) {
                "USER_UPLOADED" -> USER_UPLOADED
                "SIDECAR" -> SIDECAR
                "GENERATED" -> GENERATED
                else -> UNKNOWN
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun PosterEditContent(posterState: PosterEditState) {
    val coroutineScope = rememberCoroutineScope()
    var showFilePicker by remember { mutableStateOf(false) }

    MultipleFilePicker(show = showFilePicker) { files ->
        if (files != null) {
            coroutineScope.launch { posterState.onThumbnailUpload(files.map { Path.of(it.path) }) }
            showFilePicker = false
        }
    }

    ExternalDragAndDropArea(
        onFileUpload = { coroutineScope.launch { posterState.onThumbnailUpload(it) } },
        modifier = Modifier.sizeIn(minWidth = 500.dp, minHeight = 500.dp).fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .clickable { showFilePicker = true }
                .cursorForHand(),
            contentAlignment = Alignment.Center,
        ) {
            StripedBar(Modifier.height(100.dp).fillMaxWidth().clip(RectangleShape))
            Text(
                "Choose an image - drag and drop",
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            posterState.userUploadedThumbnails.forEach { thumb ->
                ThumbnailUploadCard(
                    thumbnail = thumb,
                    onDelete = { posterState.onUploadThumbnailDelete(thumb.path) },
                    onSelect = { posterState.onUploadThumbnailSelect(thumb.path) },
                    modifier = Modifier.width(140.dp)
                )
            }

            posterState.thumbnails.forEach { thumb ->
                ThumbnailEditCard(
                    thumbnail = thumb,
                    onDelete = { posterState.onExistingThumbnailDelete(thumb) },
                    onSelect = { posterState.onExistingThumbnailSelect(thumb) },
                    modifier = Modifier.width(140.dp)
                )
            }

        }
    }
}


@Composable
private fun StripedBar(modifier: Modifier) {
    val color = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier) {
        val step = 50.dp
        val angleDegrees = 45f
        val stepPx = step.toPx()
        val stepsCount = (size.width / stepPx).roundToInt()
        val actualStep = size.width / stepsCount
        val dotSize = Size(width = actualStep / 2, height = size.height * 2)
        for (i in -1..stepsCount) {
            val rect = Rect(
                offset = Offset(x = i * actualStep, y = (size.height - dotSize.height) / 2),
                size = dotSize,
            )
            rotate(angleDegrees, pivot = rect.center) {
                drawRect(
                    color,
                    topLeft = rect.topLeft,
                    size = rect.size,
                )
            }
        }
    }
}