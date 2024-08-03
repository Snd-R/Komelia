package io.github.snd_r.komelia.ui.settings.announcements

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import kotlinx.coroutines.launch
import snd.komga.client.announcements.KomgaAnnouncementsClient
import snd.komga.client.announcements.KomgaJsonFeed
import snd.komga.client.announcements.KomgaJsonFeed.KomgaAnnouncementId

class AnnouncementsViewModel(
    private val appNotifications: AppNotifications,
    private val announcementsClient: KomgaAnnouncementsClient
) : StateScreenModel<LoadState<KomgaJsonFeed>>(Loading) {

    init {
        screenModelScope.launch {

            appNotifications.runCatchingToNotifications {
                mutableState.value = Success(announcementsClient.getAnnouncements())
            }.onFailure { mutableState.value = Error(it) }

        }
    }

    fun markAsRead(id: KomgaAnnouncementId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            announcementsClient.markAnnouncementsRead(listOf(id))
        }
    }

}