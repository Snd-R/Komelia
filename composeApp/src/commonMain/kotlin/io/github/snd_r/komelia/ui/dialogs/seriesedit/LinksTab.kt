package io.github.snd_r.komelia.ui.dialogs.seriesedit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.ui.dialogs.LinksEditContent
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komga.common.KomgaWebLink

internal class LinksTab(
    private val vm: SeriesEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "Links",
        icon = Icons.Default.Link
    )

    @Composable
    override fun Content() {
        LinksEditContent(
            links = vm.links,
            onLinkAdd = { vm.links.add(KomgaWebLink("", "")) },
            onLinkChange = { index, link -> vm.links[index] = link },
            onLinkRemove = { index -> vm.links.removeAt(index) }
        )
    }
}

