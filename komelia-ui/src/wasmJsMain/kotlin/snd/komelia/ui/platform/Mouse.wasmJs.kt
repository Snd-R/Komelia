package snd.komelia.ui.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.BrowserCursor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

//TODO do not use internal classes
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
actual fun Modifier.cursorForMove() =
    this.pointerHoverIcon(BrowserCursor("move"))

actual fun Modifier.cursorForHand(): Modifier =
    this.pointerHoverIcon(PointerIcon.Hand)
