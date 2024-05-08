package io.github.snd_r.komelia.ui.common.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp

const val defaultCardWidth = 240

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    image: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp),
        modifier = modifier.aspectRatio(0.703f).combinedClickable(onClick = onClick ?: {}, onLongClick = onLongClick)
    ) {
        image()
    }
}

@Composable
fun ItemCardWithContent(
    modifier: Modifier,
    image: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp),
        modifier = modifier
    ) {
        Box(modifier = Modifier.aspectRatio(0.703f)) { image() }
        content()
    }
}


@Composable
fun CardGradientOverlay() {
    val colorStops = arrayOf(
        0.0f to Color.Black.copy(alpha = .5f),
        0.05f to Color.Transparent,
        0.6f to Color.Transparent,
        0.90f to Color.Black.copy(alpha = .8f),
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colorStops = colorStops))
    )
}

@Composable
fun overlayBorderModifier() =
    Modifier.border(BorderStroke(3.dp, MaterialTheme.colorScheme.tertiary), RoundedCornerShape(5.dp))


@Composable
fun cardTextStyle() = MaterialTheme.typography.bodyMedium.copy(
    color = MaterialTheme.colorScheme.primary,
    shadow = Shadow(
        color = Color.Black,
        offset = Offset(-1f, -1f),
        blurRadius = 0f
    ),
)