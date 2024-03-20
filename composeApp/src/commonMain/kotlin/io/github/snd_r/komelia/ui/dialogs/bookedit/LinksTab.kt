package io.github.snd_r.komelia.ui.dialogs.bookedit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.ui.dialogs.LinksTabContent
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komga.common.KomgaWebLink

class LinksTab(
    private val vm: BookEditDialogViewModel
) : DialogTab {
    override fun options() = TabItem(
        title = "LINKS",
        icon = Icons.Default.Link
    )

    @Composable
    override fun Content() {
        LinksTabContent(
            links = vm.links,
            onLinkAdd = { vm.links.add(KomgaWebLink("", "")) },
            onLinkChange = { index, link -> vm.links[index] = link },
            onLinkRemove = { index -> vm.links.removeAt(index) }
        )
    }
}