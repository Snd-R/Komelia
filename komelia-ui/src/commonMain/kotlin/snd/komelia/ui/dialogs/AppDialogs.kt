package snd.komelia.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import snd.komelia.ui.platform.VerticalScrollbar
import snd.komelia.ui.platform.cursorForHand
import kotlin.math.roundToInt

// FIXME starting from compose 1.8.0 Android doesn't properly display animatedContentSize inside dialog
expect val dialogAnimateContentSize: Modifier

@Composable
fun AppDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
    header: (@Composable () -> Unit)? = null,
    controlButtons: (@Composable () -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(0.dp),
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
            contentPadding = contentPadding
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
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceContainerHighest),
            shape = RoundedCornerShape(12.dp),
            color = color,
            modifier = modifier
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                .then(dialogAnimateContentSize),
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
    contentPadding: PaddingValues,
) {
    SubcomposeLayout { constraints ->
        val topPadding = contentPadding.calculateTopPadding().roundToPx()
        val bottomPadding = contentPadding.calculateBottomPadding().roundToPx()
        val leftPadding = contentPadding.calculateLeftPadding(layoutDirection).roundToPx()
        val rightPadding = contentPadding.calculateRightPadding(layoutDirection).roundToPx()
        val verticalPadding = topPadding + bottomPadding
        val horizontalPadding = leftPadding + rightPadding

        val headerPlaceable = header?.let {
            subcompose(DialogSlots.Header, header)
                .map {
                    it.measure(
                        constraints.copy(
                            minHeight = topPadding,
                            maxWidth = constraints.maxWidth - horizontalPadding,
                        )
                    )
                }.first()
        }

        val buttonsPlaceable = controlButtons?.let {
            subcompose(DialogSlots.Buttons, controlButtons)
                .map {
                    it.measure(
                        constraints.copy(
                            minWidth = 0,
                            minHeight = bottomPadding,
                            maxWidth = constraints.maxWidth - horizontalPadding
                        )
                    )
                }.first()
        }

        val headerHeight = headerPlaceable?.height ?: 0
        val buttonsHeight = buttonsPlaceable?.height ?: 0
        val dialogMaxHeight = (constraints.maxHeight * 0.9).roundToInt()

        val resizedBodyPlaceable = subcompose(DialogSlots.Body, body).map {
            it.measure(
                Constraints(
                    minHeight = 0,
                    maxHeight = (dialogMaxHeight - verticalPadding - headerHeight - buttonsHeight).coerceAtLeast(0),
                    maxWidth = constraints.maxWidth - horizontalPadding.coerceAtLeast(0)
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

        var heightUsed = 0
        layout(
            width = constraints.maxWidth,
            height = headerHeight + resizedBodyPlaceable.height + buttonsHeight + verticalPadding
        ) {

            headerPlaceable?.let {
                it.placeRelative(x = leftPadding, y = topPadding)
                heightUsed += it.height + topPadding
            }

            resizedBodyPlaceable.placeRelative(x = leftPadding, y = heightUsed)
            heightUsed += resizedBodyPlaceable.height

            buttonsPlaceable?.placeRelative(
                x = constraints.maxWidth - buttonsPlaceable.width - rightPadding,
                y = heightUsed
            )

            scrollbarPlaceable?.placeRelative(
                x = constraints.maxWidth - scrollbarPlaceable.width - 2,
                y = (headerPlaceable?.height ?: 0) + topPadding
            )
        }
    }
}

@Composable
fun DialogConfirmCancelButtons(
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    confirmEnabled: Boolean = true,
    showCancelButton: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (showCancelButton)
            ElevatedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text(cancelText)
            }

        FilledTonalButton(
            onClick = onConfirm,
            enabled = confirmEnabled,
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.cursorForHand()
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(25.dp))
            else Text(confirmText)
        }
    }
}

@Composable
fun DialogSimpleHeader(headerText: String) {
    Column {
        Text(headerText, style = MaterialTheme.typography.headlineMedium)
        HorizontalDivider(Modifier.padding(vertical = 10.dp))
    }

}

private enum class DialogSlots {
    Header,
    Body,
    Scrollbar,
    Buttons
}