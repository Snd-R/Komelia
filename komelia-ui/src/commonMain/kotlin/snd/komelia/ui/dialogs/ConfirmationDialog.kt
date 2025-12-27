package snd.komelia.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.platform.cursorForHand

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConfirmationDialog(
    body: String,
    title: String? = null,
    confirmText: String? = null,
    buttonCancel: String = "Cancel",
    buttonConfirm: String = "Confirm",
    buttonAlternate: String? = null,
    buttonConfirmColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    onDialogConfirm: () -> Unit,
    onDialogConfirmAlternate: () -> Unit = {},
    onDialogDismiss: () -> Unit,
) {
    var confirmed by remember { mutableStateOf(false) }
    AppDialog(
        onDismissRequest = onDialogDismiss,
        modifier = Modifier.widthIn(max = 600.dp),
        header = title?.let { { Text(title, fontSize = 20.sp, modifier = Modifier.padding(10.dp)) } },
        content = {
            Column(Modifier.padding(10.dp)) {
                Text(body, modifier = Modifier.padding(20.dp))
                if (confirmText != null) {
                    CheckboxWithLabel(
                        checked = confirmed,
                        onCheckedChange = { confirmed = it },
                        label = { Text(confirmText) }
                    )
                }
            }
        },
        controlButtons = {
            FlowRow(Modifier.padding(10.dp)) {
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = onDialogDismiss,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand(),
                ) {
                    Text(buttonCancel)
                }
                Spacer(Modifier.size(10.dp))

                if (buttonAlternate != null) {
                    TextButton(
                        onClick = {
                            onDialogConfirmAlternate()
                            onDialogDismiss()
                        },
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier.cursorForHand(),
                    ) {
                        Text(buttonAlternate)
                    }
                    Spacer(Modifier.size(10.dp))
                }

                FilledTonalButton(
                    onClick = {
                        onDialogConfirm()
                        onDialogDismiss()
                    },
                    enabled = confirmText == null || confirmed,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = buttonConfirmColor,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand(),
                ) {
                    Text(buttonConfirm)
                }
            }
        }
    )
}