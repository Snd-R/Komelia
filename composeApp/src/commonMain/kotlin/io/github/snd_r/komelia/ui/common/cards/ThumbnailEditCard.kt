package io.github.snd_r.komelia.ui.common.cards

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.snd_r.komelia.ui.common.images.ThumbnailImage
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.ThumbnailToBeUploaded
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.ThumbnailType.GENERATED
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.ThumbnailType.SIDECAR
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.ThumbnailType.UNKNOWN
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.ThumbnailType.USER_UPLOADED


@Composable
fun ThumbnailEditCard(
    thumbnail: KomgaThumbnail,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    ItemCardWithContent(
        modifier,
        image = { ThumbnailImage(data = thumbnail, cacheKey = thumbnail.id.value, contentScale = ContentScale.Crop) }
    ) {
        val (icon, tooltip) = when (thumbnail.type) {
            USER_UPLOADED -> Icons.Default.CloudDone to "User uploaded"
            SIDECAR -> Icons.Default.Folder to "Local artwork"
            GENERATED -> Icons.AutoMirrored.Filled.InsertDriveFile to "Generated artwork"
            UNKNOWN -> Icons.Default.Folder to ""
        }

        ThumbnailCardContent(
            onSelect = onSelect,
            isSelected = thumbnail.markedSelected,
            onDelete = if (thumbnail.type == GENERATED) null else onDelete,
            isDeleted = thumbnail.markedDeleted,
            filesize = thumbnail.fileSize,
            size = IntSize(thumbnail.width, thumbnail.height),
            mediaType = thumbnail.mediaType,
            typeIcon = icon,
            typeTooltip = tooltip
        )
    }
}

@Composable
fun ThumbnailUploadCard(
    thumbnail: ThumbnailToBeUploaded,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    ItemCardWithContent(
        modifier,
        image = { AsyncImage(model = thumbnail.path, contentDescription = null, contentScale = ContentScale.Crop) }
    ) {

        ThumbnailCardContent(
            onDelete = onDelete,
            onSelect = onSelect,
            isSelected = thumbnail.selected,
            isDeleted = false,
            filesize = thumbnail.size,
            typeIcon = Icons.Default.CloudUpload,
            typeTooltip = "To be uploaded",
            modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .3f))
        )

    }
}

@Composable
private fun ThumbnailCardContent(
    onSelect: () -> Unit,
    isSelected: Boolean,
    isDeleted: Boolean = false,
    onDelete: (() -> Unit)? = null,
    filesize: Long,
    size: IntSize? = null,
    mediaType: String? = null,
    typeIcon: ImageVector,
    typeTooltip: String,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier.padding(start = 5.dp, end = 5.dp, top = 5.dp).height(110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val sizeInKb = remember(filesize) { "%.1f".format(filesize.toFloat() / 1024) }
        Text("${sizeInKb}kB")
        size?.let { Text("w: ${it.width}, h: ${it.height}") }
        mediaType?.let { Text(it) }


        Spacer(Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconWithTooltip(tooltip = typeTooltip) {
                IconButton(onClick = {}, enabled = false) {
                    Icon(typeIcon, null)
                }
            }
            IconWithTooltip(tooltip = "Mark as selected") {
                IconButton(onClick = onSelect) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = if (isSelected) MaterialTheme.colorScheme.secondary
                        else LocalContentColor.current
                    )
                }
            }

            if (onDelete != null)
                IconWithTooltip(tooltip = "Delete") {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = if (isDeleted) MaterialTheme.colorScheme.errorContainer
                            else LocalContentColor.current
                        )
                    }
                }

        }

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun IconWithTooltip(tooltip: String, content: @Composable () -> Unit) {
    BasicTooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = .9f),
                border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(tooltip, modifier = Modifier.padding(5.dp))
            }
        },
        state = rememberBasicTooltipState()
    ) {
        content()
    }
}