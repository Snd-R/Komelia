package io.github.snd_r.komelia.offline.client

import snd.komga.client.task.KomgaTaskClient

class OfflineTaskClient : KomgaTaskClient {
    override suspend fun emptyTaskQueue(): Int {
        TODO("Not yet implemented")
    }
}