package snd.komelia.api

import snd.komelia.komga.api.KomgaActuatorApi
import snd.komga.client.actuator.KomgaActuatorClient

class RemoteActuatorApi(private val actuatorClient: KomgaActuatorClient) : KomgaActuatorApi {
    override suspend fun shutdown() = actuatorClient.shutdown()
}