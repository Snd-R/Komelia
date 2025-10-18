package io.github.snd_r.komelia.ui.common.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.images.ThumbnailImage
import snd.komf.api.metadata.KomfMetadataSeriesSearchResult

@Composable
fun KomfResultCard(
    modifier: Modifier = Modifier,
    result: KomfMetadataSeriesSearchResult,
    image: ByteArray?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {

    ResultCardOverlay(modifier = modifier, isSelected = isSelected) {
        ItemCard(
            onClick = onClick,
            containerColor =
            if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = .5f)
            else MaterialTheme.colorScheme.surfaceVariant,

            image = {
                result.imageUrl?.let {
                    ImageOverlay(isSelected) {
                        if (image != null)
                            ThumbnailImage(
                                data = image,
                                cacheKey = it,
                                contentScale = ContentScale.Crop
                            )
                    }
                }
            },
            content = { ResultDescriptionContent(result) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultDescriptionContent(result: KomfMetadataSeriesSearchResult) {
    Column(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val strings = LocalStrings.current.komf.providerSettings
        TooltipBox(
            positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            state = rememberTooltipState(),
            tooltip = {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .9f),
                    border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.surface),
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    Text(
                        result.title,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        ) {
            Text(
                text = result.title, maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.weight(1f))
        val uriHandler = LocalUriHandler.current
        ElevatedButton(
            onClick = { result.url?.let { uriHandler.openUri(it) } },
            enabled = result.url != null,
            shape = RoundedCornerShape(5.dp)
        ) {
            Text(
                text = strings.forProvider(result.provider),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ResultCardOverlay(
    modifier: Modifier,
    isSelected: Boolean,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()
    val selectionModifier = if (isSelected) {
        Modifier.border(BorderStroke(3.dp, MaterialTheme.colorScheme.secondary), RoundedCornerShape(5.dp))
    } else if (isHovered.value) overlayBorderModifier()
    else Modifier

    Box(
        modifier = modifier
            .fillMaxSize()
            .hoverable(interactionSource)
            .then(selectionModifier),
        contentAlignment = Alignment.Center
    ) {
        content()

    }
}

@Composable
private fun ImageOverlay(
    isSelected: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        content()
        if (isSelected) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondary.copy(alpha = .5f)))
        }

    }
}