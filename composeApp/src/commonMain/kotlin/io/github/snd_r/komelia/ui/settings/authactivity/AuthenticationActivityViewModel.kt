package io.github.snd_r.komelia.ui.settings.authactivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.user.KomgaAuthenticationActivity
import io.github.snd_r.komga.user.KomgaUserClient
import io.github.snd_r.komga.user.KomgaUserSort
import kotlinx.coroutines.launch

class AuthenticationActivityViewModel(
    private val forMe: Boolean,
    private val userClient: KomgaUserClient,
    private val appNotifications: AppNotifications,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    var activity by mutableStateOf<List<KomgaAuthenticationActivity>>(emptyList())
        private set
    var currentPage by mutableStateOf(1)
        private set
    var totalPages by mutableStateOf(1)
        private set
    var pageSize by mutableStateOf(20)
        private set

    fun initialize() {
        if (state.value !is LoadState.Uninitialized) return

        loadPage(1)
    }

    fun loadPage(pageNumber: Int) {
        screenModelScope.launch {
            mutableState.value = LoadState.Loading
            appNotifications.runCatchingToNotifications {
                val pageRequest = KomgaPageRequest(
                    pageIndex = pageNumber - 1,
                    size = pageSize,
                    sort = KomgaUserSort.byDateTimeDesc()
                )
                val page = if (forMe) userClient.getMeAuthenticationActivity(pageRequest)
                else userClient.getAuthenticationActivity(pageRequest)

                totalPages = page.totalPages
                currentPage = page.number + 1
                activity = page.content

                mutableState.value = LoadState.Success(Unit)
            }
        }
    }

}