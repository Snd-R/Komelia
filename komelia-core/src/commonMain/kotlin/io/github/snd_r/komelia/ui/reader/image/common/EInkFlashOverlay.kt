package io.github.snd_r.komelia.ui.reader.image.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.snd_r.komelia.ui.reader.image.ReaderFlashColor
import io.github.snd_r.komelia.ui.reader.image.ReaderFlashColor.BLACK
import io.github.snd_r.komelia.ui.reader.image.ReaderFlashColor.WHITE
import io.github.snd_r.komelia.ui.reader.image.ReaderFlashColor.WHITE_AND_BLACK
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

@Composable
fun EInkFlashOverlay(
    enabled: Boolean,
    pageChangeFlow: Flow<Unit>,
    flashEveryNPages: Int,
    flashWith: ReaderFlashColor,
    flashDuration: Long
) {
    if (!enabled) return
    var show by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(Color.Black) }
    LaunchedEffect(flashEveryNPages, flashWith, flashDuration) {
        var pagesSeen = 0
        pageChangeFlow.collect {
            pagesSeen += 1
            if (pagesSeen == flashEveryNPages) {
                color = when (flashWith) {
                    BLACK -> Color.Black
                    WHITE -> Color.White
                    WHITE_AND_BLACK -> Color.White
                }
                show = true

                if (flashWith == WHITE_AND_BLACK) {
                    delay(flashDuration / 2)
                    color = Color.Black
                    delay(flashDuration / 2)
                } else {
                    delay(flashDuration)
                }

                show = false
                pagesSeen = 0
            }
        }
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(initialAlpha = 1f),
        exit = fadeOut()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
        )
    }
}