package io.github.snd_r.komga.actuator

import io.ktor.client.*
import io.ktor.client.request.*

class KomgaActuatorClient internal constructor(private val ktor: HttpClient) {

    suspend fun shutdown() {
        ktor.post("actuator/shutdown")
    }
}