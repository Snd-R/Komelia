package io.github.snd_r.komelia.ui.dialogs.komf.identify

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.settings.komf.KomfConfigState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komf.api.KomfProviders
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.api.job.KomfMetadataJobEvent
import snd.komf.api.job.KomfMetadataJobEvent.ProcessingErrorEvent
import snd.komf.api.job.KomfMetadataJobEvent.ProviderBookEvent
import snd.komf.api.job.KomfMetadataJobEvent.ProviderErrorEvent
import snd.komf.api.job.KomfMetadataJobEvent.ProviderSeriesEvent
import snd.komf.api.job.KomfMetadataJobEvent.UnknownEvent
import snd.komf.api.job.KomfMetadataJobId
import snd.komf.api.job.KomfMetadataJobStatus
import snd.komf.api.metadata.KomfIdentifyRequest
import snd.komf.api.metadata.KomfMetadataSeriesSearchResult
import snd.komf.client.KomfJobClient
import snd.komf.client.KomfMetadataClient
import snd.komga.client.series.KomgaSeries

class KomfIdentifyDialogViewModel(
    series: KomgaSeries,
    komfMetadataClient: KomfMetadataClient,
    komfJobClient: KomfJobClient,
    private val komfConfig: KomfConfigState,
    private val appNotifications: AppNotifications,
    onDismiss: () -> Unit,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mutableState: MutableStateFlow<LoadState<Unit>> = MutableStateFlow(LoadState.Uninitialized)
    var currentTab by mutableStateOf(IdentifyTab.IDENTIFY_SETTINGS)
        private set
    val state = mutableState.asStateFlow()

    val configState = ConfigState(
        series = series,
        komfMetadataClient = komfMetadataClient,
        appNotifications = appNotifications,
        state = mutableState,
        onSearch = {
            searchResultsState.searchResults = it
            currentTab = IdentifyTab.SEARCH_RESULTS
        },
        onAutoIdentify = {
            identificationState.launchEventCollection(it)
            currentTab = IdentifyTab.IDENTIFICATION_PROGRESS
        },
        onDismiss = onDismiss,
    )
    val searchResultsState = SearchResultsState(
        series = series,
        komfMetadataClient = komfMetadataClient,
        appNotifications = appNotifications,
        onComplete = {
            identificationState.launchEventCollection(it)
            currentTab = IdentifyTab.IDENTIFICATION_PROGRESS
        },
        onDismiss = onDismiss,
    )

    val identificationState = IdentificationState(
        komfJobClient = komfJobClient,
        appNotifications = appNotifications,
        coroutineScope = coroutineScope,
        state = mutableState,
        onDismiss = onDismiss,
    )

    suspend fun initialize() {
        appNotifications.runCatchingToNotifications { komfConfig.getConfig() }
            .onFailure { mutableState.value = LoadState.Error(it) }
            .onSuccess { mutableState.value = LoadState.Success(Unit) }
    }

    fun onDispose() {
        coroutineScope.cancel()
    }

    enum class IdentifyTab {
        IDENTIFY_SETTINGS,
        SEARCH_RESULTS,
        IDENTIFICATION_PROGRESS
    }

    class IdentificationState(
        private val komfJobClient: KomfJobClient,
        private val appNotifications: AppNotifications,
        private val state: MutableStateFlow<LoadState<Unit>>,
        private val coroutineScope: CoroutineScope,
        val onDismiss: () -> Unit,
    ) {

        var providersProgress = mutableStateListOf<ProviderProgressStatus>()
            private set
        var processingError by mutableStateOf<String?>(null)
            private set
        var postProcessing by mutableStateOf(false)

        fun launchEventCollection(jobId: KomfMetadataJobId) {
            coroutineScope.launch {
                appNotifications.runCatchingToNotifications {

                    state.value = LoadState.Loading
                    val events = komfJobClient.getJobEvents(jobId)
                    events.onEach { event ->
                        when (event) {
                            KomfMetadataJobEvent.NotFound -> {
                                val job = komfJobClient.getJob(jobId)
                                when (job.status) {
                                    KomfMetadataJobStatus.RUNNING -> error("Could not get job events")
                                    KomfMetadataJobStatus.FAILED -> error("Job failed")
                                    KomfMetadataJobStatus.COMPLETED -> {
                                        onDismiss()
                                    }
                                }
                            }

                            is ProviderBookEvent -> providersProgress.addOrReplace(
                                ProviderProgressStatus(
                                    provider = event.provider,
                                    message = "Retrieving book data: ${event.bookProgress}/${event.totalBooks}",
                                    totalProgress = event.totalBooks,
                                    currentProgress = event.bookProgress,
                                    status = ProgressStatus.RUNNING,
                                )
                            )

                            is ProviderSeriesEvent -> providersProgress.addOrReplace(
                                ProviderProgressStatus(
                                    provider = event.provider,
                                    message = "Retrieving series data",
                                    totalProgress = null,
                                    currentProgress = null,
                                    status = ProgressStatus.RUNNING
                                )
                            )

                            is KomfMetadataJobEvent.ProviderCompletedEvent -> providersProgress.addOrReplace(
                                ProviderProgressStatus(
                                    provider = event.provider,
                                    message = null,
                                    totalProgress = null,
                                    currentProgress = null,
                                    status = ProgressStatus.COMPLETED
                                )
                            )

                            is ProviderErrorEvent -> providersProgress.addOrReplace(
                                ProviderProgressStatus(
                                    provider = event.provider,
                                    message = event.message,
                                    totalProgress = null,
                                    currentProgress = null,
                                    status = ProgressStatus.ERROR
                                )
                            )

                            is ProcessingErrorEvent -> {
                                processingError = event.message
                            }

                            KomfMetadataJobEvent.PostProcessingStartEvent -> postProcessing = true
                            UnknownEvent -> {}
                        }
                    }
                        .onCompletion { cause ->
                            if (cause != null) {
                                appNotifications.addErrorNotification(cause)
                                onDismiss()
                            } else {
                                postProcessing = false
                                state.value = LoadState.Success(Unit)
                            }
                        }
                        .launchIn(coroutineScope)
                }.onFailure { onDismiss() }
            }
        }

        private fun MutableList<ProviderProgressStatus>.addOrReplace(value: ProviderProgressStatus) {
            val existing = this.indexOfFirst { it.provider == value.provider }
            if (existing != -1) this[existing] = value
            else this.add(value)
        }

        data class ProviderProgressStatus(
            val provider: KomfProviders,
            val message: String?,
            val totalProgress: Int?,
            val currentProgress: Int?,
            val status: ProgressStatus
        )

        enum class ProgressStatus {
            RUNNING,
            COMPLETED,
            ERROR,
        }
    }


    class SearchResultsState(
        private val series: KomgaSeries,
        private val komfMetadataClient: KomfMetadataClient,
        private val appNotifications: AppNotifications,
        private val onComplete: (KomfMetadataJobId) -> Unit,
        val onDismiss: () -> Unit,
    ) {
        var searchResults by mutableStateOf(emptyList<KomfMetadataSeriesSearchResult>())
        var selectedSearchResult by mutableStateOf<KomfMetadataSeriesSearchResult?>(null)

        suspend fun onResultConfirm() {
            val result = selectedSearchResult ?: return

            appNotifications.runCatchingToNotifications {
                val response = komfMetadataClient.identifySeries(
                    KomfIdentifyRequest(
                        libraryId = KomfServerLibraryId(series.libraryId.value),
                        seriesId = KomfServerSeriesId(series.id.value),
                        provider = result.provider,
                        providerSeriesId = result.resultId
                    )
                )
                onComplete(response.jobId)
            }.onFailure { onDismiss() }
        }

        suspend fun getSeriesCover(result: KomfMetadataSeriesSearchResult): ByteArray? {
            return appNotifications.runCatchingToNotifications {
                komfMetadataClient.getSeriesCover(
                    libraryId = KomfServerLibraryId(series.libraryId.value),
                    provider = result.provider,
                    providerSeriesId = result.resultId
                )
            }.getOrNull()
        }
    }

    class ConfigState(
        private val series: KomgaSeries,
        private val komfMetadataClient: KomfMetadataClient,
        private val appNotifications: AppNotifications,
        private val onSearch: (List<KomfMetadataSeriesSearchResult>) -> Unit,
        private val onAutoIdentify: (KomfMetadataJobId) -> Unit,
        private val state: MutableStateFlow<LoadState<Unit>>,
        val onDismiss: () -> Unit,
    ) {
        var searchName by mutableStateOf(series.metadata.title)
        val isLoading = state.map { it is LoadState.Loading }

        suspend fun onSearch() {
            appNotifications.runCatchingToNotifications {
                state.value = LoadState.Loading

                val results = komfMetadataClient.searchSeries(
                    name = searchName,
                    libraryId = KomfServerLibraryId(series.libraryId.value),
                    seriesId = KomfServerSeriesId(series.id.value)
                )
                state.value = LoadState.Success(Unit)
                onSearch(results)
            }.onFailure { onDismiss() }
        }

        suspend fun onAutoIdentify() {
            appNotifications.runCatchingToNotifications {
                state.value = LoadState.Loading

                val response = komfMetadataClient.matchSeries(
                    libraryId = KomfServerLibraryId(series.libraryId.value),
                    seriesId = KomfServerSeriesId(series.id.value)
                )
                state.value = LoadState.Success(Unit)
                onAutoIdentify(response.jobId)
            }.onFailure { onDismiss() }
        }
    }
}