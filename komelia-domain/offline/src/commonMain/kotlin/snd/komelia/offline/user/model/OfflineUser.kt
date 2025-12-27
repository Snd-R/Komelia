package snd.komelia.offline.user.model

import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.user.KomgaAgeRestriction
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserId

data class OfflineUser(
    val id: KomgaUserId,
    val serverId: OfflineMediaServerId?,
    val email: String,
    val roles: Set<String>,
    val sharedAllLibraries: Boolean,
    val sharedLibrariesIds: Set<KomgaLibraryId>,
    val labelsAllow: Set<String>,
    val labelsExclude: Set<String>,
    val ageRestriction: KomgaAgeRestriction?,
) {
    init {
        if (id == ROOT && serverId != null) {
            throw IllegalStateException("root user can't have serverId")
        }
    }

    companion object {
        val ROOT = KomgaUserId("0")
    }

    fun toKomgaUser() = KomgaUser(
        id = this.id,
        email = this.email,
        roles = this.roles,
        sharedAllLibraries = this.sharedAllLibraries,
        sharedLibrariesIds = this.sharedLibrariesIds,
        labelsAllow = this.labelsAllow,
        labelsExclude = this.labelsExclude,
        ageRestriction = this.ageRestriction
    )
}

fun KomgaUser.toOfflineUser(serverId: OfflineMediaServerId) = OfflineUser(
    id = this.id,
    serverId = serverId,
    email = this.email,
    roles = this.roles,
    sharedAllLibraries = this.sharedAllLibraries,
    sharedLibrariesIds = this.sharedLibrariesIds,
    labelsAllow = this.labelsAllow,
    labelsExclude = this.labelsExclude,
    ageRestriction = this.ageRestriction
)
