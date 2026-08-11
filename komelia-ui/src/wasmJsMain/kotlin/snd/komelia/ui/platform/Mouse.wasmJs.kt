package snd.komelia.ui.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.fromKeyword
import androidx.compose.ui.input.pointer.pointerHoverIcon

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.cursorForMove() =
    this.pointerHoverIcon(PointerIcon.fromKeyword("move"))

actual fun Modifier.cursorForHand(): Modifier =
    this.pointerHoverIcon(PointerIcon.Hand)
