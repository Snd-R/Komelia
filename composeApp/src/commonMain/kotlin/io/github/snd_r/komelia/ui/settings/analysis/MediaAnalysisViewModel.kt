package io.github.snd_r.komelia.ui.settings.analysis

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookQuery
import io.github.snd_r.komga.book.KomgaMediaStatus
import io.github.snd_r.komga.common.KomgaPageRequest
import kotlinx.coroutines.launch

class MediaAnalysisViewModel(
    private val bookClient: KomgaBookClient,
    private val appNotifications: AppNotifications
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    var books by mutableStateOf<List<KomgaBook>>(emptyList())
    var currentPage by mutableStateOf(1)
        private set
    var totalPages by mutableStateOf(1)
        private set
    private val pageLoadSize by mutableStateOf(20)

    fun initialize() {
        if (state.value !is LoadState.Uninitialized) return
        loadPage(1)
    }

    fun loadPage(page: Int) {
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications {
                mutableState.value = LoadState.Loading
                val pageResponse = bookClient.getAllBooks(
                    KomgaBookQuery(mediaStatus = listOf(KomgaMediaStatus.ERROR, KomgaMediaStatus.UNSUPPORTED)),
                    KomgaPageRequest(page = page - 1, size = pageLoadSize)
                )

                books = pageResponse.content
                currentPage = pageResponse.number + 1
                totalPages = pageResponse.totalPages
                mutableState.value = LoadState.Success(Unit)

            }.onFailure { mutableState.value = LoadState.Error(it) }
        }
    }
}