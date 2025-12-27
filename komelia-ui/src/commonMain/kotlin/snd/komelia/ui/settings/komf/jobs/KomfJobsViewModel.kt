package snd.komelia.ui.settings.komf.jobs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.plugins.*
import io.ktor.http.*
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.ui.LoadState
import snd.komf.api.job.KomfMetadataJob
import snd.komf.api.job.KomfMetadataJobStatus
import snd.komf.client.KomfJobClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId

class KomfJobsViewModel(
    private val jobClient: KomfJobClient,
    private val seriesApi: KomgaSeriesApi?,
    private val appNotifications: AppNotifications,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    var jobs by mutableStateOf(emptyList<KomfMetadataJob>())
    var totalPages by mutableStateOf(1)
    var currentPage by mutableStateOf(1)
    var status by mutableStateOf<KomfMetadataJobStatus?>(null)

    private val seriesCache = Cache.Builder<KomgaSeriesId, KomgaSeries>().build()

    fun initialize() {
        if (state.value !is LoadState.Uninitialized) return
        loadPage(1)
    }

    suspend fun getSeries(seriesId: KomgaSeriesId): KomgaSeries? {
        return appNotifications.runCatchingToNotifications {
            try {
                seriesApi?.let { seriesCache.get(seriesId) { seriesApi.getOneSeries(seriesId) } }
            } catch (e: ClientRequestException) {
                if (e.response.status != HttpStatusCode.NotFound) throw e
                else null
            }
        }.getOrNull()
    }

    fun onStatusSelect(status: KomfMetadataJobStatus?) {
        this.status = status
        loadPage(1)
    }

    fun onDeleteAll() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            jobClient.deleteAll()
            loadPage(1)
        }
    }

    fun loadPage(pageNumber: Int) {
        currentPage = pageNumber
        appNotifications.runCatchingToNotifications(
            coroutineScope = screenModelScope,
            onSuccess = { mutableState.value = LoadState.Success(Unit) },
            onFailure = { mutableState.value = LoadState.Error(it) }
        ) {
            mutableState.value = LoadState.Loading
            val page = jobClient.getJobs(
                status = status,
                page = currentPage,
                pageSize = 30
            )
            jobs = page.content
            totalPages = page.totalPages
            currentPage = page.currentPage
        }
    }
}