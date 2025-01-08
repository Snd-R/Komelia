package io.github.snd_r.komelia.ui.color.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp

const val handleBarSize = 24f

@Composable
fun HandleBar(
    state: HandleBarState,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val handles = state.handles.collectAsState().value
    Canvas(
        modifier
            .fillMaxWidth()
            .height(handleBarSize.dp)
            .focusRequester(focusRequester)
            .focusable()
            .pointerHoverIcon(state.hoverPointerIcon.collectAsState().value)
            .pointerInput(PointerEventPass.Main) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        if (event.type == PointerEventType.Press) {
                            focusRequester.requestFocus()
                        }
                        state.onPointerEvent(event)
                        event.changes.forEach { it.consume() }
                    }
                }
            }
            .onGloballyPositioned { state.onCanvasSizeChange(it.size) }
    ) {
        handles.forEach {
            drawPath(path = it.path, color = it.color)
            drawPath(path = it.path, color = it.borderColor, style = Stroke())
        }
    }
}



