package io.github.snd_r.komelia.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.onPointerEvent(
    eventType: PointerEventType,
    pass: PointerEventPass,
    onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
) = onPointerEvent(eventType, pass, onEvent)

actual fun Modifier.cursorForHorizontalResize(): Modifier =
    this.pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))

actual fun Modifier.cursorForMove(): Modifier =
    this.pointerHoverIcon(PointerIcon(Cursor(Cursor.MOVE_CURSOR)))

actual fun Modifier.cursorForHand(): Modifier =
    this.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
