package io.github.snd_r.komelia.ui.common.cards

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.images.ReadListThumbnail
import io.github.snd_r.komelia.ui.common.menus.ReadListActionsMenu
import snd.komga.client.readlist.KomgaReadList

@Composable
fun ReadListImageCard(
    readLists: KomgaReadList,
    onCollectionClick: () -> Unit,
    onCollectionDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ItemCard(modifier, onCollectionClick) {
        ReadListCardHoverOverlay(readLists, onCollectionDelete) {
            ReadListImageOverlay(readLists) {
                ReadListThumbnail(
                    readListId = readLists.id,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun ReadListCardHoverOverlay(
    readList: KomgaReadList,
    onReadListDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()
    var isActionsMenuExpanded by remember { mutableStateOf(false) }
    val showOverlay = derivedStateOf { isHovered.value || isActionsMenuExpanded }

    val border = if (showOverlay.value) overlayBorderModifier() else Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hoverable(interactionSource)
            .then(border),
        contentAlignment = Alignment.Center
    ) {
        content()

        if (showOverlay.value) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Spacer(Modifier.weight(1f))

                Box {
                    IconButton(
                        onClick = { isActionsMenuExpanded = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) { Icon(Icons.Default.MoreVert, contentDescription = null) }

                    ReadListActionsMenu(
                        readList = readList,
                        onReadListDelete = onReadListDelete,
                        expanded = isActionsMenuExpanded,
                        onDismissRequest = { isActionsMenuExpanded = false }

                    )
                }
            }
        }
    }
}

@Composable
private fun ReadListImageOverlay(
    readlist: KomgaReadList,
    content: @Composable () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        content()
        CardGradientOverlay()
        Column(Modifier.padding(10.dp)) {
            Text(readlist.name, style = cardTextStyle())
            Text(
                if (readlist.bookIds.size == 1) "1 book" else "${readlist.bookIds.size} books",
                style = cardTextStyle()
            )
        }
    }

}