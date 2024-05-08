package io.github.snd_r.komelia.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LockIcon
import io.github.snd_r.komelia.ui.common.withTextFieldNavigation
import io.github.snd_r.komga.common.KomgaWebLink


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
            Row {
                LockIcon(linksLock, onLinksLockChange)
                TextField(
                    value = link.label,
                    onValueChange = { onLinkChange(index, link.copy(label = it)) },
                    label = { Text("Label") },
                    maxLines = 1,
                    modifier = Modifier.weight(.3f).withTextFieldNavigation()
                )

                Spacer(Modifier.size(20.dp))

                TextField(
                    value = link.url,
                    onValueChange = { onLinkChange(index, link.copy(url = it)) },
                    label = { Text("URL") },
                    maxLines = 1,
                    modifier = Modifier.weight(.7f).withTextFieldNavigation()
                )

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
