package snd.komelia.ui.platform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.BrowserCursor
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput

actual fun Modifier.onPointerEvent(
    eventType: PointerEventType,
    pass: PointerEventPass,
    onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
): Modifier = composed {
    val currentEventType by rememberUpdatedState(eventType)
    val currentOnEvent by rememberUpdatedState(onEvent)
    pointerInput(pass) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(pass)
                if (event.type == currentEventType) {
                    currentOnEvent(event)
                }
            }
        }
    }
}

//TODO
// do not use internal classes
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
actual fun Modifier.cursorForHorizontalResize(): Modifier =
    this.pointerHoverIcon(BrowserCursor("ew-resize"))

//TODO do not use internal classes
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
actual fun Modifier.cursorForMove() =
    this.pointerHoverIcon(BrowserCursor("move"))

actual fun Modifier.cursorForHand(): Modifier =
    this.pointerHoverIcon(PointerIcon.Hand)
