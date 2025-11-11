package snd.komelia

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OriginSettings(
    allowedOrigins: Collection<String>,
    allowedOriginsError: String?,
    onOriginAdd: (String) -> Unit,
    onOriginRemove: (String) -> Unit,
    newOriginError: String?
) {
    Box(Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()

        Column(
            Modifier
                .padding(10.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Permission Settings",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Allowed Origins",
                    style = MaterialTheme.typography.titleMedium,
                )
                allowedOrigins.forEach { domain ->
                    Row {
                        OutlinedTextField(
                            value = domain,
                            onValueChange = {},
                            enabled = false,
                        )
                        IconButton(
                            onClick = { onOriginRemove(domain) },
                        ) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                }
                if (allowedOriginsError != null) {
                    Text(allowedOriginsError, color = MaterialTheme.colorScheme.error)
                } else {
                    Spacer(Modifier.height(24.dp))
                }
            }

            HorizontalDivider()

            Column {
                Text(
                    text = "Add New Origin",
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var textValue by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        label = { Text("Komga url") },
                        supportingText = newOriginError?.let {
                            { Text(newOriginError, color = MaterialTheme.colorScheme.error) }
                        },
                        placeholder = {Text("https://demo.komga.org/*")}
                    )
                    FilledTonalButton(onClick = { onOriginAdd(textValue) }) {
                        Text("Add")
                    }

                }
            }

            Text("Firefox does not support port matching in url, enter hostname without port")
            Text("Due to unresolved issue with Firefox, a browser restart might be required for host permissions to apply")
        }

        VerticalScrollbar(ScrollbarAdapter(scrollState), Modifier.align(Alignment.TopEnd))
    }
}