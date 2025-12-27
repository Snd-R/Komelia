package snd.komelia.ui.settings.announcements

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaAnnouncementsApi
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komga.client.announcements.KomgaJsonFeed
import snd.komga.client.announcements.KomgaJsonFeed.KomgaAnnouncementId

class AnnouncementsViewModel(
    private val appNotifications: AppNotifications,
    private val announcementsApi: KomgaAnnouncementsApi
) : StateScreenModel<LoadState<KomgaJsonFeed>>(Loading) {

    init {
        screenModelScope.launch {

            appNotifications.runCatchingToNotifications {
                mutableState.value = Success(announcementsApi.getAnnouncements())
            }.onFailure { mutableState.value = Error(it) }

        }
    }

    fun markAsRead(id: KomgaAnnouncementId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            announcementsApi.markAnnouncementsRead(listOf(id))
        }
    }

}