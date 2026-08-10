package snd.komelia.ui.dialogs.book.edit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.edit_tab_links
import snd.komelia.ui.dialogs.LinksEditContent
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komga.client.common.KomgaWebLink

class LinksTab(
    private val vm: BookEditMetadataState
) : DialogTab {
    override fun options() = TabItem(
        title = Res.string.edit_tab_links,
        icon = Icons.Default.Link
    )

    @Composable
    override fun Content() {
        LinksEditContent(
            links = vm.links,
            linksLock = vm.linksLock,
            onLinksLockChange = vm::linksLock::set,
            onLinkAdd = { vm.links.add(KomgaWebLink("", "")) },
            onLinkChange = { index, link -> vm.links[index] = link },
            onLinkRemove = { index -> vm.links.removeAt(index) }
        )
    }
}