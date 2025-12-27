package snd.komelia.komga.api

interface KomgaTaskApi {
    suspend fun emptyTaskQueue(): Int
}