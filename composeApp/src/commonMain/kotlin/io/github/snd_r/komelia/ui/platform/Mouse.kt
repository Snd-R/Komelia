package io.github.snd_r.komelia.ui.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType

expect fun Modifier.onPointerEvent(
    eventType: PointerEventType,
    pass: PointerEventPass = PointerEventPass.Main,
    onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
): Modifier

//expect fun Modifier.onHover(
//    onHover: () -> Unit
//): Modifier

expect fun Modifier.cursorForHorizontalResize(): Modifier

expect fun Modifier.cursorForMove(): Modifier

expect fun Modifier.cursorForHand(): Modifier
