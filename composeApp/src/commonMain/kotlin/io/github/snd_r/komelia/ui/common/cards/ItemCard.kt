package io.github.snd_r.komelia.ui.common.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.platform.cursorForHand

@Composable
fun ItemCard(
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickable = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Card(
        shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp),
        modifier = modifier
            .aspectRatio(0.703f)
            .cursorForHand()
            .then(clickable)
    ) {
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
    Modifier.border(BorderStroke(3.dp, AppTheme.colors.material.tertiary), RoundedCornerShape(5.dp))


@Composable
fun cardTextStyle() = MaterialTheme.typography.bodyMedium.copy(
    color = MaterialTheme.colorScheme.primary,
    shadow = Shadow(
        color = Color.Black,
        offset = Offset(-1f, -1f),
        blurRadius = 0f
    ),
)