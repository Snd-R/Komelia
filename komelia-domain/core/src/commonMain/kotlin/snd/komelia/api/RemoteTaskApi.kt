package snd.komelia.api

import snd.komelia.komga.api.KomgaTaskApi
import snd.komga.client.task.KomgaTaskClient

class RemoteTaskApi(private val taskClient: KomgaTaskClient) : KomgaTaskApi {
    override suspend fun emptyTaskQueue()=taskClient.emptyTaskQueue()
}