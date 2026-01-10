package snd.komelia.ui.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import java.awt.Cursor

actual fun Modifier.cursorForMove(): Modifier =
    this.pointerHoverIcon(PointerIcon(Cursor(Cursor.MOVE_CURSOR)))

actual fun Modifier.cursorForHand(): Modifier =
    this.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
