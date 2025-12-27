package snd.komelia.ui.settings.authactivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaUserApi
import snd.komelia.ui.LoadState
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort
import snd.komga.client.user.KomgaAuthenticationActivity

class AuthenticationActivityViewModel(
    private val forMe: Boolean,
    private val userApi: KomgaUserApi,
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
                    sort = KomgaSort.KomgaUserSort.byDateTimeDesc()
                )
                val page = if (forMe) userApi.getMeAuthenticationActivity(pageRequest)
                else userApi.getAuthenticationActivity(pageRequest)

                totalPages = page.totalPages
                currentPage = page.number + 1
                activity = page.content

                mutableState.value = LoadState.Success(Unit)
            }
        }
    }

}