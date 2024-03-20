package io.github.snd_r.komelia.ui.dialogs.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.ALLOW_ONLY
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.EXCLUDE
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.NONE
import io.github.snd_r.komga.common.patch
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.user.AllowExclude
import io.github.snd_r.komga.user.KomgaAgeRestriction
import io.github.snd_r.komga.user.KomgaSharedLibrariesUpdate
import io.github.snd_r.komga.user.KomgaUser
import io.github.snd_r.komga.user.KomgaUserClient
import io.github.snd_r.komga.user.KomgaUserUpdateRequest
import io.github.snd_r.komga.user.ROLE_ADMIN
import io.github.snd_r.komga.user.ROLE_FILE_DOWNLOAD
import io.github.snd_r.komga.user.ROLE_PAGE_STREAMING
import io.github.snd_r.komga.user.ROLE_USER

class UserEditDialogViewModel(
    val appNotifications: AppNotifications,
    val user: KomgaUser,
    val libraries: List<KomgaLibrary>,
    private val userClient: KomgaUserClient,
) : ScreenModel {

    var administratorRole by mutableStateOf(user.roleAdmin())
    var pageStreamingRole by mutableStateOf(user.rolePageStreaming())
    var fileDownloadRole by mutableStateOf(user.roleFileDownload())

    var shareAllLibraries by mutableStateOf(user.sharedAllLibraries)
    var sharedLibraries by mutableStateOf(user.sharedLibrariesIds)
        private set

    var ageRestriction by mutableStateOf(AgeRestriction.from(user.ageRestriction))
    var ageRating by mutableStateOf(user.ageRestriction?.age ?: 0)
    var labelsAllow by mutableStateOf(user.labelsAllow)
    var labelsExclude by mutableStateOf(user.labelsExclude)


    private val userRolesTab = UserRolesTab(this)
    private val userSharedLibrariesTab = UserSharedLibrariesTab(this)
    private val userContentRestrictionTab = UserContentRestrictionTab(this)
    var currentTab by mutableStateOf<DialogTab>(userRolesTab)

    fun tabs(): List<DialogTab> {
        val tabs = mutableListOf<DialogTab>(userRolesTab)
        if (!user.roleAdmin()) {
            tabs.add(userSharedLibrariesTab)
            tabs.add(userContentRestrictionTab)
        }
        return tabs
    }

    fun addSharedLibrary(libraryId: KomgaLibraryId) {
        sharedLibraries = sharedLibraries.plus(libraryId)
    }

    fun removeSharedLibrary(libraryId: KomgaLibraryId) {
        sharedLibraries = sharedLibraries.minus(libraryId)
    }

    suspend fun saveChanges() {
        val ageRestriction = when (ageRestriction) {
            NONE -> null
            ALLOW_ONLY -> KomgaAgeRestriction(ageRating, AllowExclude.ALLOW_ONLY)
            EXCLUDE -> KomgaAgeRestriction(ageRating, AllowExclude.EXCLUDE)
        }
        val roles = buildSet {
            add(ROLE_USER)
            if (administratorRole) add(ROLE_ADMIN)
            if (fileDownloadRole) add(ROLE_FILE_DOWNLOAD)
            if (pageStreamingRole) add(ROLE_PAGE_STREAMING)
        }
        val request = KomgaUserUpdateRequest(
            ageRestriction = patch(user.ageRestriction, ageRestriction),
            labelsAllow = patch(user.labelsAllow, labelsAllow),
            labelsExclude = patch(user.labelsExclude, labelsExclude),
            roles = patch(user.roles, roles),
            sharedLibraries = patch(
                KomgaSharedLibrariesUpdate(
                    all = user.sharedAllLibraries,
                    libraryIds = user.sharedLibrariesIds
                ),
                KomgaSharedLibrariesUpdate(
                    all = shareAllLibraries,
                    libraryIds = sharedLibraries
                )
            )

        )

        appNotifications.runCatchingToNotifications {
            userClient.updateUser(user.id, request)
        }
    }

    enum class AgeRestriction(val title: String) {
        NONE("No restriction"),
        ALLOW_ONLY("Allow only Under"),
        EXCLUDE("Exclude over");

        override fun toString(): String {
            return title
        }

        companion object {
            fun from(komga: KomgaAgeRestriction?): AgeRestriction {
                return when (komga?.restriction) {
                    AllowExclude.ALLOW_ONLY -> ALLOW_ONLY
                    AllowExclude.EXCLUDE -> EXCLUDE
                    null -> NONE
                }
            }
        }


    }
}