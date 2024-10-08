package io.github.snd_r.komelia.offline.client

import snd.komga.client.announcements.KomgaAnnouncementsClient
import snd.komga.client.announcements.KomgaJsonFeed

class OfflineAnnouncementsClient : KomgaAnnouncementsClient {
    override suspend fun getAnnouncements(): KomgaJsonFeed {
        return KomgaJsonFeed("0", "", null, "", emptyList())
    }

    override suspend fun markAnnouncementsRead(announcements: List<KomgaJsonFeed.KomgaAnnouncementId>) {
    }
}