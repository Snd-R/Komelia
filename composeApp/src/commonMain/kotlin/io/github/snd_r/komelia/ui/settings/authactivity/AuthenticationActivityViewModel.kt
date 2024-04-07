package io.github.snd_r.komelia.ui.settings.authactivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.user.KomgaAuthenticationActivity
import io.github.snd_r.komga.user.KomgaUserClient
import io.github.snd_r.komga.user.KomgaUserSort
import kotlinx.coroutines.launch

class AuthenticationActivityViewModel(
    private val forMe: Boolean,
    private val userClient: KomgaUserClient,
    private val appNotifications: AppNotifications,
) : ScreenModel {
    var activity = mutableStateListOf<KomgaAuthenticationActivity>()
        private set
    var currentActivityPage by mutableStateOf(0)
        private set
    var totalPages by mutableStateOf(1)
        private set
    var totalElements by mutableStateOf(0)
        private set
    var pageNumberOfElements by mutableStateOf(0)
        private set
    var pageSize by mutableStateOf(20)


    init {
        screenModelScope.launch { loadPage(0) }
    }

    fun loadMoreEntries() {
        screenModelScope.launch {
            loadPage(currentActivityPage + 1)
        }
    }

    suspend fun onPageSizeChange(pageSize: Int) {
        this.pageSize = pageSize
        loadPage(currentActivityPage)
    }

    suspend fun loadPage(pageNumber: Int) {
        appNotifications.runCatchingToNotifications {
            val pageRequest = KomgaPageRequest(
                page = pageNumber,
                size = pageSize,
                sort = KomgaUserSort.byDateTimeDesc()
            )
            val page = if (forMe) userClient.getMeAuthenticationActivity(pageRequest)
            else userClient.getAuthenticationActivity(pageRequest)

            totalPages = page.totalPages
            currentActivityPage = page.number
            activity.addAll(page.content)
            pageNumberOfElements = page.numberOfElements
            totalElements = page.totalElements
        }
    }

}