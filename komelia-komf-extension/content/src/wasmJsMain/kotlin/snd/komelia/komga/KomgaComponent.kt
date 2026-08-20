package snd.komelia.komga

import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.HTMLElement
import snd.komelia.KomfActiveDialog
import snd.komelia.MediaServerComponent
import snd.komelia.komga.next.KomgaLibraryActionsNext
import snd.komelia.komga.next.KomgaSeriesActionsNext
import snd.komelia.komga.next.KomgaSettingsButtonNext
import snd.komelia.ui.Theme

class KomgaComponent(
    private val theme: MutableStateFlow<Theme>,
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) : MediaServerComponent {
    private val settingsButton = KomgaSettingsButton(theme = theme, currentDialog = currentDialog)
    private val seriesActions = KomgaSeriesActions(theme = theme, currentDialog = currentDialog)
    private val libraryActions = KomgaLibraryActions(theme = theme, currentDialog = currentDialog)

    private val settingsButtonNext = KomgaSettingsButtonNext(theme = theme, currentDialog = currentDialog)
    private val seriesActionsNext = KomgaSeriesActionsNext(theme = theme, currentDialog = currentDialog)
    private val libraryActionsNext = KomgaLibraryActionsNext(theme = theme, currentDialog = currentDialog)

    override fun tryMount(parentElement: HTMLElement): Boolean {
        val settingsMounted = settingsButton.tryMount(parentElement)
        val seriesMounted = seriesActions.tryMount(parentElement)
        val libraryMounted = libraryActions.tryMount(parentElement)

        val settingsNextMounted = settingsButtonNext.tryMount(parentElement)
        val seriesNextMounted = seriesActionsNext.tryMount(parentElement)
        val libraryNextMounted = libraryActionsNext.tryMount(parentElement)

        val mounted = settingsMounted || seriesMounted || libraryMounted
                || settingsNextMounted || seriesNextMounted || libraryNextMounted
        if (!mounted) return false
        setupTheme()
        return mounted
    }

    fun setupTheme() {
        theme.value = getCurrentTheme()
    }
}
