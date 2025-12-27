package snd.komelia.offline.api

import snd.komelia.komga.api.KomgaActuatorApi

class OfflineActuatorApi : KomgaActuatorApi {
    override suspend fun shutdown() = Unit
}