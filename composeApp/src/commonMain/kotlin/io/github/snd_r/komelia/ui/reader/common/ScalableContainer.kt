package io.github.snd_r.komelia.ui.reader.common

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import io.github.snd_r.komelia.platform.onPointerEvent
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun ScalableContainer(
    scaleState: ScreenScaleState,
    content: @Composable BoxScope.() -> Unit,
) {
    var isCtrlPressed by remember { mutableStateOf(false) }
    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    LaunchedEffect(Unit) { keyEvents.collect { event -> isCtrlPressed = event.isCtrlPressed } }

    val areaSize = scaleState.areaSize.collectAsState().value
    val currentTransforms = scaleState.transformation.collectAsState().value
    val areaCenter = remember(areaSize) { Offset(areaSize.width / 2f, areaSize.height / 2f) }

    val flingScope = rememberCoroutineScope()
    val flingSpec = rememberSplineBasedDecay<Offset>()
    var isFlinging by remember { mutableStateOf(false) }
    val scrollOrientation = scaleState.scrollOrientation.collectAsState().value

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .graphicsLayer {
                scaleX = currentTransforms.scale
                scaleY = currentTransforms.scale
                translationX = currentTransforms.offset.x
                translationY = currentTransforms.offset.y
            }
            .pointerInput(areaSize) {
                coroutineScope {
                    launch {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            if (isFlinging) {
                                flingScope.coroutineContext.cancelChildren()
                                isFlinging = false
                                down.consume()
                            }
                        }
                    }
                    launch {
                        detectDragGestures(
                            onDragEnd = {
                                flingScope.launch {
                                    isFlinging = true
                                    scaleState.performFling(flingSpec)
                                    isFlinging = false
                                }
                            },
                            onDrag = { change, dragAmount ->
                                scaleState.addPan(change, dragAmount)
                            }
                        )
                    }
                    launch {
                        detectTransformGestures { centroid, _, zoom, _ ->
                            scaleState.multiplyZoom(zoom, centroid - areaCenter)
                        }
                    }
                }
            }
            .onPointerEvent(PointerEventType.Scroll) {
                val delta = it.changes[0].scrollDelta
                if (isCtrlPressed) {
                    val centroid = it.changes[0].position
                    scaleState.addZoom(.2f * -delta.y, centroid - areaCenter)
                } else {
                    val pan = -delta.y * 70
                    when (scrollOrientation) {
                        Vertical, null -> scaleState.addPan(Offset(0f, pan))
                        Horizontal -> scaleState.addPan(Offset(pan, 0f))
                    }

                }
            }
    ) {
        content()

    }
}
