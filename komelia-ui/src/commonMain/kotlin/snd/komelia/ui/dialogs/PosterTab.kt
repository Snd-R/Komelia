package snd.komelia.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import snd.komelia.ui.common.cards.ThumbnailEditCard
import snd.komelia.ui.common.cards.ThumbnailUploadCard
import snd.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.ThumbnailToBeUploaded
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komelia.ui.platform.ExternalDragAndDropArea
import snd.komelia.ui.platform.cursorForHand
import snd.komga.client.book.KomgaBookThumbnail
import snd.komga.client.collection.KomgaCollectionThumbnail
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.readlist.KomgaReadListThumbnail
import snd.komga.client.series.KomgaSeriesThumbnail
import kotlin.math.roundToInt

class PosterTab(private val state: PosterEditState) : DialogTab {

    override fun options() = TabItem(
        title = "POSTER",
        icon = Icons.Default.Image,
    )

    @Composable
    override fun Content() {
        val widthState = state.cardWidth.collectAsState(null)

        val cardWidth = widthState.value
        if (cardWidth != null) PosterEditContent(posterState = state, cardWidth = cardWidth)
    }
}

class PosterEditState(
    val cardWidth: Flow<Dp>,
) {
    var thumbnails by mutableStateOf<List<KomgaThumbnail>>(emptyList())
    var userUploadedThumbnails by mutableStateOf<List<ThumbnailToBeUploaded>>(emptyList())

    fun onThumbnailUpload(files: List<PlatformFile>) {
        val newFiles = files.map { ThumbnailToBeUploaded(true, it) }

        if (newFiles.isNotEmpty()) {
            thumbnails = thumbnails.map { it.copy(selected = false) }

            val toUpload = mutableListOf<ThumbnailToBeUploaded>()
            toUpload.addAll(userUploadedThumbnails.map { it.copy(selected = false) })
            toUpload.addAll(newFiles.dropLast(1))
            toUpload.add(newFiles.last().copy(selected = true))
            userUploadedThumbnails = toUpload
        }
    }

    fun onExistingThumbnailSelect(thumbnail: KomgaThumbnail) {
        thumbnails = thumbnails.map {
            if (it.id == thumbnail.id) it.copy(selected = true, deleted = false)
            else it.copy(selected = false)
        }
        userUploadedThumbnails = userUploadedThumbnails.map { it.copy(selected = false) }
    }

    fun onExistingThumbnailDelete(thumb: KomgaThumbnail) {
        thumbnails = thumbnails.map {
            if (it.id == thumb.id) it.copy(deleted = !it.markedDeleted, selected = false)
            else it
        }
    }

    fun onUploadThumbnailDelete(thumb: ThumbnailToBeUploaded) {
        userUploadedThumbnails = userUploadedThumbnails.filter { it != thumb }
    }

    fun onUploadThumbnailSelect(thumb: ThumbnailToBeUploaded) {
        thumbnails = thumbnails.map { it.copy(selected = false) }

        userUploadedThumbnails = userUploadedThumbnails.map {
            if (it == thumb) it.copy(selected = true)
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

        class CollectionThumbnail(
            val komgaThumbnail: KomgaCollectionThumbnail,
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
                return CollectionThumbnail(komgaThumbnail, selected, deleted)
            }
        }

        class ReadListThumbnail(
            val komgaThumbnail: KomgaReadListThumbnail,
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
                return ReadListThumbnail(komgaThumbnail, selected, deleted)
            }
        }

        data class ThumbnailToBeUploaded(
            val selected: Boolean = true,
            val file: PlatformFile,
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
fun PosterEditContent(
    posterState: PosterEditState,
    cardWidth: Dp,
) {
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        title = "Choose a file",
    ) { files ->
        files?.let { posterState.onThumbnailUpload(it) }
    }

    ExternalDragAndDropArea(
        onFileUpload = { coroutineScope.launch { posterState.onThumbnailUpload(it) } },
        modifier = Modifier.sizeIn(minWidth = 500.dp, minHeight = 500.dp).fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .clickable { launcher.launch() }
                .cursorForHand(),
            contentAlignment = Alignment.Center,
        ) {
            StripedBar(
                Modifier
                    .background(MaterialTheme.colorScheme.surfaceDim)
                    .height(100.dp)
                    .fillMaxWidth()
                    .clip(RectangleShape)
            )
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
                    onDelete = { posterState.onUploadThumbnailDelete(thumb) },
                    onSelect = { posterState.onUploadThumbnailSelect(thumb) },
                    modifier = Modifier.width(cardWidth)
                )
            }

            posterState.thumbnails.forEach { thumb ->
                ThumbnailEditCard(
                    thumbnail = thumb,
                    onDelete = { posterState.onExistingThumbnailDelete(thumb) },
                    onSelect = { posterState.onExistingThumbnailSelect(thumb) },
                    modifier = Modifier.width(cardWidth)
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