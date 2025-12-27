package snd.komelia.db.offline

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.tables.OfflineUserLibrarySharingTable
import snd.komelia.db.offline.tables.OfflineUserRoleTable
import snd.komelia.db.offline.tables.OfflineUserSharingTable
import snd.komelia.db.offline.tables.OfflineUserTable
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komelia.offline.user.model.OfflineUser
import snd.komelia.offline.user.repository.OfflineUserRepository
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.user.AllowExclude.ALLOW_ONLY
import snd.komga.client.user.AllowExclude.EXCLUDE
import snd.komga.client.user.KomgaAgeRestriction
import snd.komga.client.user.KomgaUserId

class ExposedOfflineUserRepository(database: Database) : ExposedRepository(database), OfflineUserRepository {
    override suspend fun save(user: OfflineUser) {
        transaction {
            OfflineUserTable.upsert {
                it[OfflineUserTable.id] = user.id.value
                it[OfflineUserTable.serverId] = user.serverId?.value
                it[OfflineUserTable.email] = user.email
                it[OfflineUserTable.sharedAllLibraries] = user.sharedAllLibraries
                it[OfflineUserTable.ageRestriction] = user.ageRestriction?.age
                it[OfflineUserTable.ageRestrictionAllowOnly] = user.ageRestriction?.restriction == ALLOW_ONLY
            }
            OfflineUserRoleTable.deleteWhere { OfflineUserRoleTable.userId.eq(user.id.value) }
            OfflineUserLibrarySharingTable.deleteWhere { OfflineUserLibrarySharingTable.userId.eq(user.id.value) }
            OfflineUserSharingTable.deleteWhere { OfflineUserSharingTable.userId.eq(user.id.value) }

            OfflineUserRoleTable.batchInsert(user.roles) { role ->
                this[OfflineUserRoleTable.userId] = user.id.value
                this[OfflineUserRoleTable.role] = role
            }
            OfflineUserLibrarySharingTable.batchInsert(user.sharedLibrariesIds) { libraryId ->
                this[OfflineUserLibrarySharingTable.userId] = user.id.value
                this[OfflineUserLibrarySharingTable.libraryId] = libraryId.value
            }
            OfflineUserSharingTable.batchInsert(user.labelsAllow) { label ->
                this[OfflineUserSharingTable.userId] = user.id.value
                this[OfflineUserSharingTable.label] = label
                this[OfflineUserSharingTable.allow] = true
            }
            OfflineUserSharingTable.batchInsert(user.labelsExclude) { label ->
                this[OfflineUserSharingTable.userId] = user.id.value
                this[OfflineUserSharingTable.label] = label
                this[OfflineUserSharingTable.allow] = false
            }
        }
    }

    override suspend fun get(id: KomgaUserId): OfflineUser {
        return find(id) ?: throw IllegalStateException("user with id $id does not exist")
    }

    override suspend fun find(id: KomgaUserId): OfflineUser? {
        return transaction {
            OfflineUserTable
                .selectAll()
                .where { OfflineUserTable.id.eq(id.value) }
                .fetchAndMap()
                .firstOrNull()
        }
    }

    override suspend fun findAll(): List<OfflineUser> {
        return transaction {
            OfflineUserTable
                .selectAll()
                .fetchAndMap()
        }
    }

    override suspend fun findAllByServer(serverId: OfflineMediaServerId): List<OfflineUser> {
        return transaction {
            OfflineUserTable
                .selectAll()
                .where { OfflineUserTable.serverId.eq(serverId.value) }
                .fetchAndMap()
        }
    }

    override suspend fun delete(id: KomgaUserId) {
        transaction {
            OfflineUserRoleTable.deleteWhere { OfflineUserRoleTable.userId.eq(id.value) }
            OfflineUserLibrarySharingTable.deleteWhere { OfflineUserLibrarySharingTable.userId.eq(id.value) }
            OfflineUserSharingTable.deleteWhere { OfflineUserSharingTable.userId.eq(id.value) }
            OfflineUserTable.deleteWhere { OfflineUserTable.id.eq(id.value) }
        }
    }

    private fun selectRoles(userIds: List<String>): Map<String, List<String>> {
        return OfflineUserRoleTable
            .selectAll()
            .where { OfflineUserRoleTable.userId.inList(userIds) }
            .groupBy({ it[OfflineUserRoleTable.userId] }, { it[OfflineUserRoleTable.role] })
    }

    private fun selectSharedLibraries(userIds: List<String>): Map<String, List<KomgaLibraryId>> {
        return OfflineUserLibrarySharingTable
            .selectAll()
            .where { OfflineUserLibrarySharingTable.userId.inList(userIds) }
            .groupBy(
                { it[OfflineUserLibrarySharingTable.userId] },
                { KomgaLibraryId(it[OfflineUserLibrarySharingTable.libraryId]) }
            )
    }

    private fun selectLabels(userIds: List<String>): Map<String, List<LabelRecord>> {
        return OfflineUserSharingTable
            .selectAll()
            .where { OfflineUserSharingTable.userId.inList(userIds) }
            .groupBy(
                { it[OfflineUserSharingTable.userId] },
                {
                    LabelRecord(
                        it[OfflineUserSharingTable.allow],
                        it[OfflineUserSharingTable.label],
                    )
                }
            )
    }

    private data class LabelRecord(val allow: Boolean, val label: String)

    private fun Query.fetchAndMap(): List<OfflineUser> {
        val rows = this.toList()
        val userIds = rows.map { it[OfflineUserTable.id] }
        val roles = selectRoles(userIds)
        val sharedLibraries = selectSharedLibraries(userIds)
        val labels = selectLabels(userIds)

        return rows.map { row ->
            val userId = row[OfflineUserTable.id]
            row.toModel(
                roles = roles[userId].orEmpty(),
                sharedLibraries = sharedLibraries[userId].orEmpty(),
                labelsAllow = labels[userId].orEmpty().filter { it.allow }.map { it.label },
                labelsExclude = labels[userId].orEmpty().filter { !it.allow }.map { it.label },
            )
        }
    }

    private fun ResultRow.toModel(
        roles: List<String>,
        sharedLibraries: List<KomgaLibraryId>,
        labelsAllow: List<String>,
        labelsExclude: List<String>,
    ): OfflineUser {
        val ageRestrictionAge = this[OfflineUserTable.ageRestriction]
        val ageRestrictionAllowOnly = this[OfflineUserTable.ageRestrictionAllowOnly]
        val ageRestriction =
            if (ageRestrictionAge != null && ageRestrictionAllowOnly != null) {
                KomgaAgeRestriction(
                    age = ageRestrictionAge,
                    restriction = if (ageRestrictionAllowOnly) ALLOW_ONLY else EXCLUDE
                )
            } else null
        return OfflineUser(
            id = KomgaUserId(this[OfflineUserTable.id]),
            serverId = this[OfflineUserTable.serverId]?.let { OfflineMediaServerId(it) },
            email = this[OfflineUserTable.email],
            roles = roles.toSet(),
            sharedAllLibraries = this[OfflineUserTable.sharedAllLibraries],
            sharedLibrariesIds = sharedLibraries.toSet(),
            labelsAllow = labelsAllow.toSet(),
            labelsExclude = labelsExclude.toSet(),
            ageRestriction = ageRestriction
        )
    }
}