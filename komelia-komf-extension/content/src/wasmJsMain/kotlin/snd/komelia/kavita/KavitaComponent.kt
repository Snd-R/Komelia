package snd.komelia.kavita

import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.HTMLElement
import snd.komelia.KomfActiveDialog
import snd.komelia.MediaServerComponent
import snd.komelia.ui.Theme

class KavitaComponent(
    private val theme: MutableStateFlow<Theme>,
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) : MediaServerComponent {
    private val settingsButton = KavitaSettingsButton(currentDialog = currentDialog)
    private val seriesActions = KavitaSeriesActions(currentDialog = currentDialog)
    private val libraryActions = KavitaLibraryActions(currentDialog = currentDialog)

    fun launch() {
        theme.value = Theme.DARK
    }


    override fun tryMount(parentElement: HTMLElement): Boolean {
        val settingsMounted = settingsButton.tryMount(parentElement)
        val libraryActionsMounted = libraryActions.tryMount(parentElement)
        val seriesActionsMounted = seriesActions.tryMount(parentElement)
        val mounted = settingsMounted || libraryActionsMounted || seriesActionsMounted

        return mounted
    }
}
