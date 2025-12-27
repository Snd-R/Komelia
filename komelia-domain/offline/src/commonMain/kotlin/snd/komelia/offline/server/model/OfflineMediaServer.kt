package snd.komelia.offline.server.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class OfflineMediaServerId(val value: String)

data class OfflineMediaServer(
    @OptIn(ExperimentalUuidApi::class)
    val id: OfflineMediaServerId = OfflineMediaServerId(Uuid.generateV4().toHexDashString()),
    val url: String
)