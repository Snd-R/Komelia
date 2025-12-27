package snd.komelia.api

import snd.komelia.komga.api.KomgaAnnouncementsApi
import snd.komga.client.announcements.KomgaAnnouncementsClient
import snd.komga.client.announcements.KomgaJsonFeed.KomgaAnnouncementId

class RemoteAnnouncementsApi(private val announcementsClient: KomgaAnnouncementsClient) : KomgaAnnouncementsApi {
    override suspend fun getAnnouncements() = announcementsClient.getAnnouncements()

    override suspend fun markAnnouncementsRead(announcements: List<KomgaAnnouncementId>) =
        announcementsClient.markAnnouncementsRead(announcements)
}