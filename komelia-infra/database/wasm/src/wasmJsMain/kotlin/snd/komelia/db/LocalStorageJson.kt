package snd.komelia.db

import kotlinx.serialization.json.Json

object LocalStorageJson {
    val json = Json {
        ignoreUnknownKeys = true
    }
}