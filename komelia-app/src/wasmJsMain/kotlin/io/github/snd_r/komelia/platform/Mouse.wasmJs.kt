package io.github.snd_r.komelia.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType

actual fun Modifier.onPointerEvent(
    eventType: PointerEventType,
    pass: PointerEventPass,
    onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
) = this

actual fun Modifier.cursorForHorizontalResize(): Modifier = this
actual fun Modifier.cursorForMove() = this
actual fun Modifier.cursorForHand() = this
