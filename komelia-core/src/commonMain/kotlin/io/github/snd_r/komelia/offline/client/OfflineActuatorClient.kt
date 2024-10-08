package io.github.snd_r.komelia.offline.client

import snd.komga.client.actuator.KomgaActuatorClient

class OfflineActuatorClient : KomgaActuatorClient {
    override suspend fun shutdown() {}
}