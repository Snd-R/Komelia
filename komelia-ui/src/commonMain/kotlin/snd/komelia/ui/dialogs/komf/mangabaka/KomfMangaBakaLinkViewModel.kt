package snd.komelia.ui.dialogs.komf.mangabaka

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import snd.komelia.AppNotifications
import snd.komelia.ManagedKomgaEvents
import snd.komelia.komga.api.model.KomeliaSeries
import snd.komelia.ui.LoadState
import snd.komf.api.KomfServerSeriesId
import snd.komf.api.mangabaka.KomfMangaBakaSeries
import snd.komf.api.mangabaka.MangaBakaSeriesId
import snd.komf.client.KomfMangaBakaClient
import snd.komga.client.sse.KomgaEvent

class KomfMangaBakaLinkViewModel(
    val series: KomeliaSeries,
    private val mangaBakaClient: KomfMangaBakaClient,
    private val appNotifications: AppNotifications,
    private val appEvents: ManagedKomgaEvents,
    private val onDismiss: () -> Unit,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val state = MutableStateFlow<LoadState<Unit>>(LoadState.Success(Unit))
    val currentTab = MutableStateFlow(BakaLinkTab.SEARCH)
    val searchResults = MutableStateFlow<List<KomfMangaBakaSeries>>(emptyList())
    val selectedResult = MutableStateFlow<KomfMangaBakaSeries?>(null)

    fun onDispose() {
        coroutineScope.cancel()
    }

    fun onSearch(title: String) {
        appNotifications.runCatchingToNotifications(
            coroutineScope = coroutineScope,
            onFailure = { state.value = LoadState.Error(it) }
        ) {
            state.value = LoadState.Loading
            searchResults.value = mangaBakaClient.search(title)
            currentTab.value = BakaLinkTab.SEARCH_RESULTS
            state.value = LoadState.Success(Unit)
        }
    }

    fun onLink(bakaSeries: KomfMangaBakaSeries) {
        onLink(bakaSeries.id)
    }

    fun onLink(mangaBakaSeriesId: MangaBakaSeriesId) {
        appNotifications.runCatchingToNotifications(
            coroutineScope = coroutineScope,
            onFailure = { state.value = LoadState.Error(it) }
        ) {
            state.value = LoadState.Loading
            mangaBakaClient.link(KomfServerSeriesId(this.series.id.value), mangaBakaSeriesId)
            appEvents.emitEvent(KomgaEvent.SeriesChanged(series.id, series.libraryId))
            state.value = LoadState.Success(Unit)
            onDismiss()
        }
    }

    fun onResultSelect(result: KomfMangaBakaSeries) {
        selectedResult.value = result
    }

    enum class BakaLinkTab {
        SEARCH,
        SEARCH_RESULTS
    }
}