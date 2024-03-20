package io.github.snd_r.komelia.ui.dialogs.tabs

import androidx.compose.runtime.Composable

interface DialogTab {
    fun options(): TabItem

    @Composable
    fun Content()
}