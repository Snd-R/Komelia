package io.github.snd_r.komelia.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LockIcon
import io.github.snd_r.komelia.ui.common.withTextFieldNavigation
import snd.komga.client.common.KomgaWebLink


@Composable
fun LinksEditContent(
    links: List<KomgaWebLink>,
    linksLock: Boolean,
    onLinksLockChange: (Boolean) -> Unit,
    onLinkAdd: () -> Unit,
    onLinkChange: (index: Int, title: KomgaWebLink) -> Unit,
    onLinkRemove: (index: Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .heightIn(min = 100.dp)
            .fillMaxWidth()
    ) {
        links.forEachIndexed { index, link ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                LockIcon(linksLock, onLinksLockChange)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    TextField(
                        value = link.label,
                        onValueChange = { onLinkChange(index, link.copy(label = it)) },
                        label = { Text("Label") },
                        maxLines = 2,
                        modifier = Modifier.widthIn(min = 100.dp).withTextFieldNavigation()
                    )

                    TextField(
                        value = link.url,
                        onValueChange = { onLinkChange(index, link.copy(url = it)) },
                        label = { Text("URL") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().withTextFieldNavigation()
                    )
                }
                IconButton(onClick = { onLinkRemove(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }

        FilledTonalIconButton(onClick = onLinkAdd) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}
