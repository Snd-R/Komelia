package io.github.snd_r.komelia.ui.reader.view

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import io.github.snd_r.komelia.platform.onPointerEvent
import io.github.snd_r.komelia.ui.reader.PageSpreadScaleState.Transformation
import io.github.snd_r.komelia.ui.reader.PagedReaderPageState
import kotlin.math.pow

@Composable
fun ScalableContainer(
    pageState: PagedReaderPageState,
    isCtrlPressed: Boolean,
    content: @Composable BoxScope.() -> Unit,
) {
    val areaSize = pageState.containerSize.collectAsState().value ?: IntSize.Zero
    val areaCenter = Offset(areaSize.width / 2f, areaSize.height / 2f)
    val currentSpread = pageState.currentSpread.collectAsState().value
    val scaleTransformations = currentSpread.scaleState?.transformation?.collectAsState()?.value
        ?: Transformation(Offset.Zero, 1f)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scaleTransformations.scale
                scaleY = scaleTransformations.scale
                translationX = scaleTransformations.offset.x
                translationY = scaleTransformations.offset.y
            }

            .pointerInput(areaSize) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    pageState.addPan(pan)
                    pageState.addZoom(zoom, centroid - areaCenter)
                }
            }
            .onPointerEvent(PointerEventType.Scroll) {
                val delta = it.changes[0].scrollDelta
                if (isCtrlPressed) {
                    val centroid = it.changes[0].position
                    val zoom = 1.2f.pow(-delta.y)
                    pageState.addZoom(zoom, centroid - areaCenter)
                } else {
                    val pan = -delta.y * 50
                    pageState.addPan(Offset(0f, pan))
                }
            }
    ) {
        content()

    }
}
