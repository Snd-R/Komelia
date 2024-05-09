package io.github.snd_r.komelia.ui.dialogs.book.editbulk

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LockableChipTextField
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komga.common.KomgaAuthor

class AuthorsTab(
    private val vm: BookBulkEditDialogViewModel
) : DialogTab {
    override fun options() = TabItem(
        title = "AUTHORS",
        icon = Icons.Default.People
    )

    @Composable
    override fun Content() {
        AuthorsTabContent(
            authors = vm.authors,
            onAuthorsRoleGroupChange = { role, authors -> vm.authors = vm.authors.plus(role to authors) },
            authorsLock = vm.authorsLock,
            onLockChange = vm::authorsLock::set
        )
    }
}

@Composable
private fun AuthorsTabContent(
    authors: Map<String, List<KomgaAuthor>>,
    onAuthorsRoleGroupChange: (role: String, authors: List<KomgaAuthor>) -> Unit,
    authorsLock: Boolean,
    onLockChange: (Boolean) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {

        val warningColor = MaterialTheme.colorScheme.tertiary
        Row(Modifier.border(Dp.Hairline, warningColor).padding(20.dp)) {
            Icon(Icons.Default.PriorityHigh, null, tint = warningColor)
            Text(
                text = "You are editing authors for multiple books. This will override existing authors of each book.",
                color = warningColor
            )
        }

        authors.forEach { (role, authors) ->
            LockableChipTextField(
                values = authors.map { it.name },
                onValuesChange = { newAuthors ->
                    onAuthorsRoleGroupChange(
                        role,
                        newAuthors.map { KomgaAuthor(role = role, name = it) })
                },
                label = role,
                locked = authorsLock,
                onLockChange = onLockChange
            )
        }

        var newCustomRole by remember { mutableStateOf("") }
        TextField(
            value = newCustomRole,
            onValueChange = { newCustomRole = it },
            label = { Text("Add custom role") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .padding(start = 50.dp)
                .onPreviewKeyEvent {
                    if (it.type == KeyEventType.KeyDown && it.key == Key.Enter) {
                        if (newCustomRole.isNotBlank()) {
                            onAuthorsRoleGroupChange(newCustomRole.trim(), emptyList())
                            newCustomRole = ""
                        }

                        return@onPreviewKeyEvent true
                    }
                    false
                },
            trailingIcon = {
                IconButton(onClick = {
                    if (newCustomRole.isNotBlank()) {
                        onAuthorsRoleGroupChange(newCustomRole.trim(), emptyList())
                        newCustomRole = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        )
    }
}

