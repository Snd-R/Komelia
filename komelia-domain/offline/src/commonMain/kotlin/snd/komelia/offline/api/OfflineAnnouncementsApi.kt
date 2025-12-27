package snd.komelia.offline.api

import snd.komelia.komga.api.KomgaAnnouncementsApi
import snd.komga.client.announcements.KomgaJsonFeed
import snd.komga.client.announcements.KomgaJsonFeed.KomgaAnnouncementId

class OfflineAnnouncementsApi : KomgaAnnouncementsApi {
    override suspend fun getAnnouncements(): KomgaJsonFeed = KomgaJsonFeed("", "", null, null)

    override suspend fun markAnnouncementsRead(announcements: List<KomgaAnnouncementId>) = Unit
}