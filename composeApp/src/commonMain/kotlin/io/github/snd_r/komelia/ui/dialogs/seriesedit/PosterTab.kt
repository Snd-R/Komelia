package io.github.snd_r.komelia.ui.dialogs.seriesedit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem

internal class PosterTab(
    private val vm: SeriesEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "POSTER",
        icon = Icons.Default.Image,
        enabled = false
    )

    @Composable
    override fun Content() {
    }
}