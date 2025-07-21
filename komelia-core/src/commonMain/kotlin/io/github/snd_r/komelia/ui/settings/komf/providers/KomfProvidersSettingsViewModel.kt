package io.github.snd_r.komelia.ui.settings.komf.providers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.settings.komf.KomfSharedState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komf.api.KomfCoreProviders.ANILIST
import snd.komf.api.KomfCoreProviders.BANGUMI
import snd.komf.api.KomfCoreProviders.BOOK_WALKER
import snd.komf.api.KomfCoreProviders.COMIC_VINE
import snd.komf.api.KomfCoreProviders.HENTAG
import snd.komf.api.KomfCoreProviders.KODANSHA
import snd.komf.api.KomfCoreProviders.MAL
import snd.komf.api.KomfCoreProviders.MANGADEX
import snd.komf.api.KomfCoreProviders.MANGA_BAKA
import snd.komf.api.KomfCoreProviders.MANGA_UPDATES
import snd.komf.api.KomfCoreProviders.NAUTILJON
import snd.komf.api.KomfCoreProviders.VIZ
import snd.komf.api.KomfCoreProviders.WEBTOONS
import snd.komf.api.KomfCoreProviders.YEN_PRESS
import snd.komf.api.KomfNameMatchingMode
import snd.komf.api.KomfProviders
import snd.komf.api.PatchValue.Some
import snd.komf.api.UnknownKomfProvider
import snd.komf.api.config.AniListConfigUpdateRequest
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.config.MangaBakaConfigUpdateRequest
import snd.komf.api.config.MangaBakaDatabaseDto
import snd.komf.api.config.MangaBakaDownloadProgress
import snd.komf.api.config.MangaDexConfigUpdateRequest
import snd.komf.api.config.MetadataProvidersConfigUpdateRequest
import snd.komf.api.config.ProviderConfigUpdateRequest
import snd.komf.api.config.ProvidersConfigDto
import snd.komf.api.config.ProvidersConfigUpdateRequest
import snd.komf.api.mediaserver.KomfMediaServerLibraryId
import snd.komf.client.KomfConfigClient

class KomfProvidersSettingsViewModel(
    private val komfConfigClient: KomfConfigClient,
    private val appNotifications: AppNotifications,
    val komfSharedState: KomfSharedState,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    private val komgaLibraries = komfSharedState.getKomgaLibraries()
    private val kavitaLibraries = komfSharedState.getKavitaLibraries()
    val libraries = komgaLibraries.combine(kavitaLibraries) { komga, kavita ->
        if (komga.isNotEmpty() && kavita.isNotEmpty()) {
            komga.map { it.copy(name = "${it.name} (Komga)") }
                .plus(kavita.map { it.copy(name = "${it.name} (Kavita)") })
        } else {
            komga.plus(kavita)
        }
    }
    var defaultProvidersConfig by mutableStateOf(ProvidersConfigState(this::updateConfig, null, null))
        private set
    var libraryProvidersConfigs by mutableStateOf<Map<KomfMediaServerLibraryId, ProvidersConfigState>>(emptyMap())
        private set

    var comicVineClientId by mutableStateOf<String?>(null)
        private set
    var malClientId by mutableStateOf<String?>(null)
        private set
    var nameMatchingMode by mutableStateOf(KomfNameMatchingMode.CLOSEST_MATCH)
        private set
    var mangaBakaDbMetadata by mutableStateOf<MangaBakaDatabaseDto?>(null)
        private set

    suspend fun initialize() {
        appNotifications.runCatchingToNotifications { komfSharedState.getConfig() }
            .onFailure { mutableState.value = LoadState.Error(it) }
            .onSuccess { config ->
                mutableState.value = LoadState.Success(Unit)
                config.onEach { initFields(it) }.launchIn(screenModelScope)
            }
    }

    private fun initFields(config: KomfConfig) {
        defaultProvidersConfig =
            ProvidersConfigState(this::updateConfig, null, config.metadataProviders.defaultProviders)
        libraryProvidersConfigs = config.metadataProviders.libraryProviders
            .map { (libraryId, config) ->
                val komgaLibraryId = KomfMediaServerLibraryId(libraryId)
                komgaLibraryId to ProvidersConfigState(this::updateConfig, komgaLibraryId, config)
            }.toMap()
        comicVineClientId = config.metadataProviders.comicVineClientId
        malClientId = config.metadataProviders.malClientId
        nameMatchingMode = config.metadataProviders.nameMatchingMode
        mangaBakaDbMetadata = config.metadataProviders.mangaBakaDatabase
    }

    private fun updateConfig(request: MetadataProvidersConfigUpdateRequest) {
        val configUpdate = KomfConfigUpdateRequest(metadataProviders = Some(request))
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications { komfConfigClient.updateConfig(configUpdate) }
                .onFailure { initFields(komfSharedState.getConfig().first()) }
        }
    }

    fun onNewLibraryTabAdd(libraryId: KomfMediaServerLibraryId) {
        libraryProvidersConfigs = libraryProvidersConfigs.plus(
            libraryId to ProvidersConfigState(this::updateConfig, libraryId, null)
        )
        val providersUpdate = MetadataProvidersConfigUpdateRequest(
            libraryProviders = Some(mapOf(libraryId.value to ProvidersConfigUpdateRequest()))
        )
        updateConfig(providersUpdate)
    }

    fun onLibraryTabRemove(libraryId: KomfMediaServerLibraryId) {
        libraryProvidersConfigs = libraryProvidersConfigs.minus(libraryId)

        val providersUpdate = MetadataProvidersConfigUpdateRequest(
            libraryProviders = Some(mapOf(libraryId.value to null))
        )
        updateConfig(providersUpdate)
    }

    fun onNameMatchingModeChange(mode: KomfNameMatchingMode) {
        this.nameMatchingMode = mode
        val providersUpdate = MetadataProvidersConfigUpdateRequest(nameMatchingMode = Some(mode))
        updateConfig(providersUpdate)
    }

    fun onComicVineClientIdChange(clientId: String) {
        this.comicVineClientId = clientId
        val providersUpdate = MetadataProvidersConfigUpdateRequest(comicVineClientId = Some(clientId))
        updateConfig(providersUpdate)
    }

    fun onMalClientIdChange(clientId: String) {
        this.malClientId = clientId
        val providersUpdate = MetadataProvidersConfigUpdateRequest(malClientId = Some(clientId))
        updateConfig(providersUpdate)
    }

    fun onMangaBakaDbUpdate(): Flow<MangaBakaDownloadProgress> {
        return komfConfigClient.updateMangaBakaDb()
    }

    class ProvidersConfigState(
        private val onMetadataUpdate: (MetadataProvidersConfigUpdateRequest) -> Unit,
        private val libraryId: KomfMediaServerLibraryId?,
        config: ProvidersConfigDto?,
    ) {
        private val aniList = AniListConfigState(ANILIST, config?.aniList, this::onAniListConfigUpdate)
        private val bangumi = GenericProviderConfigState(BANGUMI, config?.bangumi, this::onProviderConfigUpdate)
        private val bookWalker =
            GenericProviderConfigState(BOOK_WALKER, config?.bookWalker, this::onProviderConfigUpdate)
        private val comicVine = GenericProviderConfigState(COMIC_VINE, config?.comicVine, this::onProviderConfigUpdate)
        private val hentag = GenericProviderConfigState(HENTAG, config?.hentag, this::onProviderConfigUpdate)
        private val kodansha = GenericProviderConfigState(KODANSHA, config?.kodansha, this::onProviderConfigUpdate)
        private val mal = GenericProviderConfigState(MAL, config?.mal, this::onProviderConfigUpdate)
        private val mangaUpdates =
            GenericProviderConfigState(MANGA_UPDATES, config?.mangaUpdates, this::onProviderConfigUpdate)
        private val mangaBaka = MangaBakaConfigState(MANGA_BAKA, config?.mangaBaka, this::onMangaBakaConfigUpdate)
        private val mangaDex = MangaDexConfigState(MANGADEX, config?.mangaDex, this::onMangaDexConfigUpdate)
        private val nautiljon = GenericProviderConfigState(NAUTILJON, config?.nautiljon, this::onProviderConfigUpdate)
        private val yenPress = GenericProviderConfigState(YEN_PRESS, config?.yenPress, this::onProviderConfigUpdate)
        private val viz = GenericProviderConfigState(VIZ, config?.viz, this::onProviderConfigUpdate)
        private val webtoons = GenericProviderConfigState(WEBTOONS, config?.webtoons, this::onProviderConfigUpdate)

        var enabledProviders by mutableStateOf<List<ProviderConfigState>>(
            config?.let { config ->
                listOfNotNull(
                    if (config.aniList.enabled) aniList else null,
                    if (config.bangumi.enabled) bangumi else null,
                    if (config.bookWalker.enabled) bookWalker else null,
                    if (config.comicVine.enabled) comicVine else null,
                    if (config.hentag.enabled) hentag else null,
                    if (config.kodansha.enabled) kodansha else null,
                    if (config.mal.enabled) mal else null,
                    if (config.mangaUpdates.enabled) mangaUpdates else null,
                    if (config.mangaDex.enabled) mangaDex else null,
                    if (config.mangaBaka.enabled) mangaBaka else null,
                    if (config.nautiljon.enabled) nautiljon else null,
                    if (config.yenPress.enabled) yenPress else null,
                    if (config.viz.enabled) viz else null,
                    if (config.webtoons.enabled) webtoons else null,
                ).sortedBy { it.priority }
            } ?: emptyList()
        )
            private set

        fun onProviderReorder(fromIndex: Int, toIndex: Int) {
            val mutable = enabledProviders.toMutableList()
            val movedValue = mutable.removeAt(fromIndex)
            mutable.add(toIndex, movedValue)
            mutable.forEachIndexed { index, value -> value.onPriorityChange(index + 1) }
            enabledProviders = mutable
        }

        fun onProviderAdd(provider: KomfProviders) {
            val configState = when (provider) {
                ANILIST -> aniList
                BANGUMI -> bangumi
                BOOK_WALKER -> bookWalker
                COMIC_VINE -> comicVine
                HENTAG -> hentag
                KODANSHA -> kodansha
                MAL -> mal
                MANGA_UPDATES -> mangaUpdates
                MANGADEX -> mangaDex
                NAUTILJON -> nautiljon
                YEN_PRESS -> yenPress
                VIZ -> viz
                MANGA_BAKA -> mangaBaka
                WEBTOONS -> webtoons
                is UnknownKomfProvider -> error("Can't add config for unknown provider ${provider.name}")
            }

            enabledProviders = enabledProviders.plus(configState)
            configState.onPriorityChange(enabledProviders.size)
            configState.onEnabledChange(true)
        }

        fun onProviderRemove(state: ProviderConfigState) {
            enabledProviders = enabledProviders.minus(state)
            enabledProviders.forEachIndexed { index, value -> value.onPriorityChange(index + 1) }
            state.onEnabledChange(false)
        }


        private fun onAniListConfigUpdate(config: AniListConfigUpdateRequest) {
            val aniListUpdate = ProvidersConfigUpdateRequest(aniList = Some(config))
            val providersUpdate = if (libraryId == null) {
                MetadataProvidersConfigUpdateRequest(defaultProviders = Some(aniListUpdate))
            } else {
                MetadataProvidersConfigUpdateRequest(libraryProviders = Some(mapOf(libraryId.value to aniListUpdate)))
            }
            onMetadataUpdate(providersUpdate)
        }

        private fun onMangaDexConfigUpdate(config: MangaDexConfigUpdateRequest) {
            val mangaDexUpdate = ProvidersConfigUpdateRequest(mangaDex = Some(config))
            val providersUpdate = if (libraryId == null) {
                MetadataProvidersConfigUpdateRequest(defaultProviders = Some(mangaDexUpdate))
            } else {
                MetadataProvidersConfigUpdateRequest(libraryProviders = Some(mapOf(libraryId.value to mangaDexUpdate)))
            }
            onMetadataUpdate(providersUpdate)
        }

        private fun onMangaBakaConfigUpdate(config: MangaBakaConfigUpdateRequest) {
            val mangaBakaUpdate = ProvidersConfigUpdateRequest(mangaBaka = Some(config))
            val providersUpdate = if (libraryId == null) {
                MetadataProvidersConfigUpdateRequest(defaultProviders = Some(mangaBakaUpdate))
            } else {
                MetadataProvidersConfigUpdateRequest(libraryProviders = Some(mapOf(libraryId.value to mangaBakaUpdate)))
            }
            onMetadataUpdate(providersUpdate)
        }

        private fun onProviderConfigUpdate(config: ProviderConfigUpdateRequest, provider: KomfProviders) {
            val update = when (provider) {
                BANGUMI -> ProvidersConfigUpdateRequest(bangumi = Some(config))
                BOOK_WALKER -> ProvidersConfigUpdateRequest(bookWalker = Some(config))
                COMIC_VINE -> ProvidersConfigUpdateRequest(comicVine = Some(config))
                HENTAG -> ProvidersConfigUpdateRequest(hentag = Some(config))
                KODANSHA -> ProvidersConfigUpdateRequest(kodansha = Some(config))
                MAL -> ProvidersConfigUpdateRequest(mal = Some(config))
                MANGA_UPDATES -> ProvidersConfigUpdateRequest(mangaUpdates = Some(config))
                NAUTILJON -> ProvidersConfigUpdateRequest(nautiljon = Some(config))
                YEN_PRESS -> ProvidersConfigUpdateRequest(yenPress = Some(config))
                VIZ -> ProvidersConfigUpdateRequest(viz = Some(config))
                WEBTOONS -> ProvidersConfigUpdateRequest(webtoons = Some(config))
                MANGADEX, ANILIST, MANGA_BAKA, is UnknownKomfProvider -> error("Unexpected provider $provider")
            }

            val providersUpdate = if (libraryId == null) {
                MetadataProvidersConfigUpdateRequest(defaultProviders = Some(update))
            } else {
                MetadataProvidersConfigUpdateRequest(libraryProviders = Some(mapOf(libraryId.value to update)))
            }
            onMetadataUpdate(providersUpdate)
        }

    }

}
