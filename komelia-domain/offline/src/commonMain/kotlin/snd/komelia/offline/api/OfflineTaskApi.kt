package snd.komelia.offline.api

import snd.komelia.komga.api.KomgaTaskApi

class OfflineTaskApi : KomgaTaskApi {
    override suspend fun emptyTaskQueue(): Int = 0
}