package io.github.snd_r.komelia.ui.reader.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun NavigationHelpDialog(
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),

            ) {
            Row {
                KeyDescriptionColumn(
                    "Reader Navigation",
                    mapOf(
                        listOf("←") to "Previous page",
                        listOf("→") to "Next page",
                        listOf("Home") to "First page",
                        listOf("End") to "Last page",
                        listOf("Ctrl", "Scroll Wheel") to "Zoom"
                    ),
                    Modifier.weight(1f)
                )

                KeyDescriptionColumn(
                    "Settings",
                    mapOf(
                        listOf("L") to "Left to Right",
                        listOf("R") to "Right to Left",
                        listOf("C") to "Cycle scale",
                        listOf("D") to "Cycle page layout",
                        listOf("O") to "Toggle double page offset",
                        listOf("F11") to "Enter/exit full screen"
                    ),
                    Modifier.weight(1f)
                )

                KeyDescriptionColumn(
                    "Menus",
                    mapOf(
                        listOf("M") to "Show/hide menu",
                        listOf("H") to "Show/hide help",
                        listOf("← Backspace") to "Return to series screen"
                    ),
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KeyDescriptionColumn(
    title: String,
    keyToDescription: Map<List<String>, String>,
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.padding(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Row {
            Text(
                text = "Key",
//                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Description",
                modifier = Modifier.weight(1f)
            )
        }

        keyToDescription.forEach { (keys, description) ->
            HorizontalDivider()
            Row {

                ShortcutKeys(keys, Modifier.weight(1f))
                Text(description, Modifier.weight(1f))
            }
        }

    }
}

@Composable
private fun ShortcutKeys(keys: List<String>, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        ShortcutKey(keys.first())
        keys.drop(1).forEach { key ->
            Text(" + ")
            ShortcutKey(key)
        }
    }
}

@Composable
private fun ShortcutKey(label: String) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontWeight = FontWeight.Bold
        )
    }
}