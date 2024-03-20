package io.github.snd_r.komelia.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.withTextFieldKeyMapping
import io.github.snd_r.komelia.ui.platform.ScrollBarConfig
import io.github.snd_r.komelia.ui.platform.verticalScrollWithScrollbar
import io.github.snd_r.komga.common.KomgaWebLink


@Composable
fun LinksTabContent(
    links: List<KomgaWebLink>,
    onLinkAdd: () -> Unit,
    onLinkChange: (index: Int, title: KomgaWebLink) -> Unit,
    onLinkRemove: (index: Int) -> Unit,
) {

    val scrollState = rememberScrollState()
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .heightIn(min = 100.dp, max = 600.dp)
            .fillMaxWidth()
            .verticalScrollWithScrollbar(
                state = scrollState,
                scrollbarConfig = ScrollBarConfig(
                    indicatorColor = MaterialTheme.colorScheme.onSurface,
                    alpha = .8f
                )
            )
    ) {
        links.forEachIndexed { index, link ->
            Row {
                TextField(
                    value = link.label,
                    onValueChange = { onLinkChange(index, link.copy(label = it)) },
                    label = { Text("Label") },
                    modifier = Modifier.weight(.3f).withTextFieldKeyMapping()
                )

                Spacer(Modifier.size(20.dp))

                TextField(
                    value = link.url,
                    onValueChange = { onLinkChange(index, link.copy(url = it)) },
                    label = { Text("URL") },
                    modifier = Modifier.weight(.7f).withTextFieldKeyMapping()
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
