package io.github.snd_r.komga.user

import io.github.snd_r.komga.common.PatchValue
import io.github.snd_r.komga.library.KomgaLibraryId
import kotlinx.serialization.Serializable

const val ROLE_USER = "USER"
const val ROLE_ADMIN = "ADMIN"
const val ROLE_FILE_DOWNLOAD = "FILE_DOWNLOAD"
const val ROLE_PAGE_STREAMING = "PAGE_STREAMING"

@Serializable
@JvmInline
value class KomgaUserId(val value: String) {
    override fun toString() = value
}

@Serializable
data class KomgaUser(
    val id: KomgaUserId,
    val email: String,
    val roles: Set<String>,
    val sharedAllLibraries: Boolean,
    val sharedLibrariesIds: Set<KomgaLibraryId>,
    val labelsAllow: Set<String>,
    val labelsExclude: Set<String>,
    val ageRestriction: KomgaAgeRestriction?
) {
    fun roleAdmin() = roles.contains("ADMIN")
    fun roleFileDownload() = roles.contains("FILE_DOWNLOAD")
    fun rolePageStreaming() = roles.contains("PAGE_STREAMING")
}

@Serializable
data class KomgaUserCreateRequest(
    val email: String,
    val password: String,
    val roles: Set<String>
)

@Serializable
data class KomgaUserUpdateRequest(
    val ageRestriction: PatchValue<KomgaAgeRestriction> = PatchValue.Unset,
    val labelsAllow: PatchValue<Set<String>> = PatchValue.Unset,
    val labelsExclude: PatchValue<Set<String>> = PatchValue.Unset,
    val roles: PatchValue<Set<String>> = PatchValue.Unset,
    val sharedLibraries: PatchValue<KomgaSharedLibrariesUpdate> = PatchValue.Unset,
)

@Serializable
data class KomgaSharedLibrariesUpdate(
    val all: Boolean,
    val libraryIds: Set<KomgaLibraryId>
)


@Serializable
data class KomgaAgeRestriction(
    val age: Int,
    val restriction: AllowExclude
)

enum class AllowExclude {
    ALLOW_ONLY, EXCLUDE,
}

