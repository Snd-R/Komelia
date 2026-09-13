package snd.komelia.ui.reader.image.continuous

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import snd.komelia.ui.dialogs.AppDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContinuousShortcutsDialog(
    keyBindings: ContinuousKeyBindings,
    onKeyBindingsChange: (ContinuousKeyBindings) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var capturingForAction by remember { mutableStateOf<ContinuousShortcutAction?>(null) }

    AppDialog(
        modifier = Modifier.fillMaxWidth(.6f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Configure Scroll Shortcuts", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Click 'Add key' and press the desired key. Keys already bound to other actions will be reassigned.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.weight(1f))

                ContinuousShortcutAction.entries.forEach { action ->
                    val isCapturing = capturingForAction == action
                    val boundKeys = keyBindings.bindings[action] ?: emptySet()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (action) {
                                ContinuousShortcutAction.SCROLL_UP -> "Scroll Up"
                                ContinuousShortcutAction.SCROLL_DOWN -> "Scroll Down"
                            },
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(2f)
                        ) {
                            boundKeys.forEach { keyCode ->
                                val keyName = keyNameForKeyCode(keyCode)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(keyName, style = MaterialTheme.typography.labelMedium)
                                        IconButton(
                                            onClick = {
                                                val newBindings = keyBindings.copy(
                                                    bindings = keyBindings.bindings.mapValues { (act, keys) ->
                                                        if (act == action) keys - keyCode else keys
                                                    }.filterValues { it.isNotEmpty() }
                                                )
                                                onKeyBindingsChange(newBindings)
                                            },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { capturingForAction = action },
                            enabled = !isCapturing
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (boundKeys.isEmpty()) "Add key" else "Add another")
                        }

                        if (isCapturing) {
                            // Invisible capture box
                            BoxWithKeyCapture(
                                onKeyCaptured = { capturedKey ->
                                    // Remove this key from any other action first
                                    val cleanedBindings = keyBindings.bindings.mapValues { (act, keys) ->
                                        keys.filter { it != capturedKey.keyCode }
                                    }
                                    val newBindings = ContinuousKeyBindings(
                                        bindings = cleanedBindings + (action to (cleanedBindings[action] ?: emptySet()) + capturedKey.keyCode)
                                    )
                                    onKeyBindingsChange(newBindings)
                                    capturingForAction = null
                                },
                                onCancel = { capturingForAction = null }
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        onKeyBindingsChange(ContinuousKeyBindings())
                    }) {
                        Text("Reset to defaults")
                    }

                    Button(onClick = onDismissRequest) {
                        Text("Close")
                    }
                }
            }
        }
    )
}

@Composable
private fun BoxWithKeyCapture(
    onKeyCaptured: (Key) -> Unit,
    onCancel: () -> Unit,
) {
    var captureText by remember { mutableStateOf("Press a key...") }

    Card(
        modifier = Modifier
            .padding(start = 8.dp)
            .onKeyEvent { event ->
                if (event.type == KeyDown) {
                    onKeyCaptured(event.key)
                    true
                } else {
                    false
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Text(
            captureText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun keyNameForKeyCode(keyCode: Long): String {
    return try {
        Key(keyCode.toInt()).keyLabel ?: "Key $keyCode"
    } catch (e: Exception) {
        "Key $keyCode"
    }
}
