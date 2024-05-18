package io.github.snd_r.komelia.ui.dialogs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.snd_r.komelia.platform.VerticalScrollbar
import kotlin.math.roundToInt

@Composable
fun AppDialog(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
    header: (@Composable () -> Unit)? = null,
    controlButtons: (@Composable () -> Unit)? = null,
) {
    BasicAppDialog(modifier, onDismissRequest, color) {
        val scrollState = rememberScrollState()
        AppDialogLayout(
            header = header,
            body = {
                Box(Modifier.verticalScroll(scrollState)) { content() }
            },
            controlButtons = controlButtons,
            scrollbar = { VerticalScrollbar(scrollState) },
        )
    }
}

@Composable
fun BasicAppDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,

        )
    ) {
        val focusManager = LocalFocusManager.current
        Surface(
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            color = color,
            modifier = modifier
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                .animateContentSize(),
            content = content
        )
    }
}

@Composable
fun AppDialogLayout(
    body: @Composable () -> Unit,
    scrollbar: @Composable () -> Unit,
    header: (@Composable () -> Unit)? = null,
    controlButtons: (@Composable () -> Unit)? = null,
) {
    SubcomposeLayout { constraints ->
        val headerPlaceable = header?.let {
            subcompose(DialogSlots.Header, header)
                .map { it.measure(constraints.copy(minHeight = 0)) }.first()
        }
        val buttonsPlaceable = controlButtons?.let {
            subcompose(DialogSlots.Buttons, controlButtons)
                .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }.first()
        }

        val dialogMaxHeight = (constraints.maxHeight * 0.9).roundToInt()

        val resizedBodyPlaceable = subcompose(DialogSlots.Body, body).map {
            val maxHeight = dialogMaxHeight - (headerPlaceable?.height ?: 0) - (buttonsPlaceable?.height ?: 0)
            it.measure(
                Constraints(
                    minHeight = 0,
                    maxHeight = maxHeight,
                    maxWidth = constraints.maxWidth
                )
            )
        }.first()

        val scrollbarPlaceable = subcompose(DialogSlots.Scrollbar, scrollbar)
            .map {
                it.measure(
                    constraints.copy(
                        minWidth = 0,
                        minHeight = 0,
                        maxHeight = resizedBodyPlaceable.height
                    )
                )
            }
            .firstOrNull()

        layout(
            width = constraints.maxWidth,
            height = (headerPlaceable?.height ?: 0) + resizedBodyPlaceable.height + (buttonsPlaceable?.height ?: 0)
        ) {

            headerPlaceable?.placeRelative(0, 0)

            resizedBodyPlaceable.placeRelative(0, headerPlaceable?.height ?: 0)

            buttonsPlaceable?.placeRelative(
                constraints.maxWidth - buttonsPlaceable.width,
                (headerPlaceable?.height ?: 0) + resizedBodyPlaceable.height
            )
            scrollbarPlaceable?.placeRelative(
                constraints.maxWidth - scrollbarPlaceable.width - 2, headerPlaceable?.height ?: 0
            )
        }
    }

}

private enum class DialogSlots {
    Header,
    Body,
    Scrollbar,
    Buttons
}