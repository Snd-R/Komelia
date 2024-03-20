package io.github.snd_r.komga.task

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class KomgaTaskClient internal constructor(private val ktor: HttpClient) {

    suspend fun emptyTaskQueue(): Int {
        return ktor.delete("api/v1/tasks").bodyAsText().toInt()
    }
}