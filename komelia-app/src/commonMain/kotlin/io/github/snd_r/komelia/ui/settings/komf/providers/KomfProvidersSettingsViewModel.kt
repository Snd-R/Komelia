package io.github.snd_r.komelia.ui.settings.komf.providers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.settings.komf.KomfConfigState
import io.github.snd_r.komelia.ui.settings.komf.providers.KomfProvidersSettingsViewModel.ProviderConfigState.AniListConfigState
import io.github.snd_r.komelia.ui.settings.komf.providers.KomfProvidersSettingsViewModel.ProviderConfigState.GenericProviderConfigState
import io.github.snd_r.komelia.ui.settings.komf.providers.KomfProvidersSettingsViewModel.ProviderConfigState.MangaDexConfigState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komf.api.KomfAuthorRole
import snd.komf.api.KomfAuthorRole.COLORIST
import snd.komf.api.KomfAuthorRole.COVER
import snd.komf.api.KomfAuthorRole.INKER
import snd.komf.api.KomfAuthorRole.LETTERER
import snd.komf.api.KomfAuthorRole.PENCILLER
import snd.komf.api.KomfAuthorRole.WRITER
import snd.komf.api.KomfMediaType
import snd.komf.api.KomfNameMatchingMode
import snd.komf.api.KomfProviders
import snd.komf.api.KomfProviders.ANILIST
import snd.komf.api.KomfProviders.BANGUMI
import snd.komf.api.KomfProviders.BOOK_WALKER
import snd.komf.api.KomfProviders.COMIC_VINE
import snd.komf.api.KomfProviders.KODANSHA
import snd.komf.api.KomfProviders.MAL
import snd.komf.api.KomfProviders.MANGADEX
import snd.komf.api.KomfProviders.MANGA_UPDATES
import snd.komf.api.KomfProviders.NAUTILJON
import snd.komf.api.KomfProviders.VIZ
import snd.komf.api.KomfProviders.YEN_PRESS
import snd.komf.api.MangaDexLink
import snd.komf.api.PatchValue
import snd.komf.api.PatchValue.Some
import snd.komf.api.config.AniListConfigDto
import snd.komf.api.config.AniListConfigUpdateRequest
import snd.komf.api.config.BookMetadataConfigUpdateRequest
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.config.MangaDexConfigDto
import snd.komf.api.config.MangaDexConfigUpdateRequest
import snd.komf.api.config.MetadataProvidersConfigUpdateRequest
import snd.komf.api.config.ProviderConf
import snd.komf.api.config.ProviderConfigUpdateRequest
import snd.komf.api.config.ProvidersConfigDto
import snd.komf.api.config.ProvidersConfigUpdateRequest
import snd.komf.api.config.SeriesMetadataConfigUpdateRequest
import snd.komf.client.KomfConfigClient
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId

class KomfProvidersSettingsViewModel(
    private val komfConfigClient: KomfConfigClient,
    private val appNotifications: AppNotifications,
    val komfConfig: KomfConfigState,
    val libraries: StateFlow<List<KomgaLibrary>>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    var defaultProvidersConfig by mutableStateOf(ProvidersConfigState(this::updateConfig, null, null))
        private set
    var libraryProvidersConfigs by mutableStateOf<Map<KomgaLibraryId, ProvidersConfigState>>(emptyMap())
        private set

    var comicVineClientId by mutableStateOf<String?>(null)
        private set
    var malClientId by mutableStateOf<String?>(null)
        private set
    var nameMatchingMode by mutableStateOf(KomfNameMatchingMode.CLOSEST_MATCH)
        private set

    suspend fun initialize() {
        appNotifications.runCatchingToNotifications { komfConfig.getConfig() }
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
                val komgaLibraryId = KomgaLibraryId(libraryId)
                komgaLibraryId to ProvidersConfigState(this::updateConfig, komgaLibraryId, config)
            }.toMap()
        comicVineClientId = config.metadataProviders.comicVineClientId
        malClientId = config.metadataProviders.malClientId
        nameMatchingMode = config.metadataProviders.nameMatchingMode
    }

    private fun updateConfig(request: MetadataProvidersConfigUpdateRequest) {
        val configUpdate = KomfConfigUpdateRequest(metadataProviders = Some(request))
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications { komfConfigClient.updateConfig(configUpdate) }
                .onFailure { initFields(komfConfig.getConfig().first()) }
        }
    }

    fun onNewLibraryTabAdd(libraryId: KomgaLibraryId) {
        libraryProvidersConfigs = libraryProvidersConfigs.plus(
            libraryId to ProvidersConfigState(this::updateConfig, libraryId, null)
        )
        val providersUpdate = MetadataProvidersConfigUpdateRequest(
            libraryProviders = Some(mapOf(libraryId.value to ProvidersConfigUpdateRequest()))
        )
        updateConfig(providersUpdate)
    }

    fun onLibraryTabRemove(libraryId: KomgaLibraryId) {
        libraryProvidersConfigs = libraryProvidersConfigs.minus(libraryId)

        val providersUpdate = MetadataProvidersConfigUpdateRequest(
            libraryProviders = Some(mapOf(libraryId.value to null))
        )
        updateConfig(providersUpdate)
    }

    class ProvidersConfigState(
        private val onMetadataUpdate: (MetadataProvidersConfigUpdateRequest) -> Unit,
        private val libraryId: KomgaLibraryId?,
        config: ProvidersConfigDto?,
    ) {
        private val aniList = AniListConfigState(ANILIST, config?.aniList, this::onAniListConfigUpdate)
        private val bangumi = GenericProviderConfigState(BANGUMI, config?.bangumi, this::onProviderConfigUpdate)
        private val bookWalker =
            GenericProviderConfigState(BOOK_WALKER, config?.bookWalker, this::onProviderConfigUpdate)
        private val comicVine = GenericProviderConfigState(COMIC_VINE, config?.comicVine, this::onProviderConfigUpdate)
        private val kodansha = GenericProviderConfigState(KODANSHA, config?.kodansha, this::onProviderConfigUpdate)
        private val mal = GenericProviderConfigState(MAL, config?.mal, this::onProviderConfigUpdate)
        private val mangaUpdates =
            GenericProviderConfigState(MANGA_UPDATES, config?.mangaUpdates, this::onProviderConfigUpdate)
        private val mangaDex = MangaDexConfigState(MANGADEX, config?.mangaDex, this::onMangaDexConfigUpdate)
        private val nautiljon = GenericProviderConfigState(NAUTILJON, config?.nautiljon, this::onProviderConfigUpdate)
        private val yenPress = GenericProviderConfigState(YEN_PRESS, config?.yenPress, this::onProviderConfigUpdate)
        private val viz = GenericProviderConfigState(VIZ, config?.viz, this::onProviderConfigUpdate)

        var enabledProviders by mutableStateOf<List<ProviderConfigState>>(
            config?.let { config ->
                listOfNotNull(
                    if (config.aniList.enabled) aniList else null,
                    if (config.bangumi.enabled) bangumi else null,
                    if (config.bookWalker.enabled) bookWalker else null,
                    if (config.comicVine.enabled) comicVine else null,
                    if (config.kodansha.enabled) kodansha else null,
                    if (config.mal.enabled) mal else null,
                    if (config.mangaUpdates.enabled) mangaUpdates else null,
                    if (config.mangaDex.enabled) mangaDex else null,
                    if (config.nautiljon.enabled) nautiljon else null,
                    if (config.yenPress.enabled) yenPress else null,
                    if (config.viz.enabled) viz else null,
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
                KODANSHA -> kodansha
                MAL -> mal
                MANGA_UPDATES -> mangaUpdates
                MANGADEX -> mangaDex
                NAUTILJON -> nautiljon
                YEN_PRESS -> yenPress
                VIZ -> viz
            }

            enabledProviders = enabledProviders.plus(configState)
            configState.onPriorityChange(enabledProviders.size + 1)
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

        private fun onProviderConfigUpdate(config: ProviderConfigUpdateRequest, provider: KomfProviders) {
            val update = when (provider) {
                BANGUMI -> ProvidersConfigUpdateRequest(bangumi = Some(config))
                BOOK_WALKER -> ProvidersConfigUpdateRequest(bookWalker = Some(config))
                COMIC_VINE -> ProvidersConfigUpdateRequest(comicVine = Some(config))
                KODANSHA -> ProvidersConfigUpdateRequest(kodansha = Some(config))
                MAL -> ProvidersConfigUpdateRequest(mal = Some(config))
                MANGA_UPDATES -> ProvidersConfigUpdateRequest(mangaUpdates = Some(config))
                NAUTILJON -> ProvidersConfigUpdateRequest(nautiljon = Some(config))
                YEN_PRESS -> ProvidersConfigUpdateRequest(yenPress = Some(config))
                VIZ -> ProvidersConfigUpdateRequest(viz = Some(config))
                MANGADEX, ANILIST -> error("Unexpected provider $provider")
            }

            val providersUpdate = if (libraryId == null) {
                MetadataProvidersConfigUpdateRequest(defaultProviders = Some(update))
            } else {
                MetadataProvidersConfigUpdateRequest(libraryProviders = Some(mapOf(libraryId.value to update)))
            }
            onMetadataUpdate(providersUpdate)
        }

    }

    sealed class ProviderConfigState(
        config: ProviderConf?,
        val provider: KomfProviders,
    ) {
        var priority by mutableStateOf(config?.priority ?: 1)
            private set
        var enabled by mutableStateOf(config?.enabled ?: false)
            private set
        var nameMatchingMode by mutableStateOf(config?.nameMatchingMode)
            private set
        var mediaType by mutableStateOf(config?.mediaType)
            private set
        var authorRoles by mutableStateOf(config?.authorRoles?.toList() ?: listOf(WRITER))
            private set
        var artistRoles by mutableStateOf(
            config?.artistRoles?.toList()
                ?: listOf(PENCILLER, INKER, COLORIST, LETTERER, COVER)
        )
            private set

        var seriesAgeRating by mutableStateOf(config?.seriesMetadata?.ageRating ?: true)
            private set
        var seriesAuthors by mutableStateOf(config?.seriesMetadata?.authors ?: true)
            private set
        var seriesCover by mutableStateOf(config?.seriesMetadata?.thumbnail ?: true)
            private set
        var seriesGenres by mutableStateOf(config?.seriesMetadata?.genres ?: true)
            private set
        var seriesLinks by mutableStateOf(config?.seriesMetadata?.links ?: true)
            private set
        var seriesPublisher by mutableStateOf(config?.seriesMetadata?.publisher ?: true)
            private set
        var seriesOriginalPublisher by mutableStateOf(config?.seriesMetadata?.useOriginalPublisher ?: true)
            private set
        var seriesReleaseDate by mutableStateOf(config?.seriesMetadata?.releaseDate ?: true)
            private set
        var seriesStatus by mutableStateOf(config?.seriesMetadata?.status ?: true)
            private set
        var seriesSummary by mutableStateOf(config?.seriesMetadata?.summary ?: true)
            private set
        var seriesTags by mutableStateOf(config?.seriesMetadata?.tags ?: true)
            private set
        var seriesTitle by mutableStateOf(config?.seriesMetadata?.title ?: true)
            private set
        var seriesBookCount by mutableStateOf(config?.seriesMetadata?.totalBookCount ?: true)
            private set

        val isBookMetadataAvailable = when (provider) {
            ANILIST, MAL, MANGA_UPDATES -> false
            else -> true
        }
        val canHaveMultiplePublishers = when (provider) {
            MANGA_UPDATES, NAUTILJON -> true
            else -> false
        }

        var bookEnabled by mutableStateOf(config?.seriesMetadata?.books ?: true)
            private set
        var bookAuthors by mutableStateOf(config?.bookMetadata?.authors ?: true)
            private set
        var bookCover by mutableStateOf(config?.bookMetadata?.thumbnail ?: true)
            private set
        var bookIsbn by mutableStateOf(config?.bookMetadata?.isbn ?: true)
            private set
        var bookLinks by mutableStateOf(config?.bookMetadata?.links ?: true)
            private set
        var bookNumber by mutableStateOf(config?.bookMetadata?.number ?: true)
            private set
        var bookReleaseDate by mutableStateOf(config?.bookMetadata?.releaseDate ?: true)
            private set
        var bookSummary by mutableStateOf(config?.bookMetadata?.summary ?: true)
            private set
        var bookTags by mutableStateOf(config?.bookMetadata?.tags ?: true)
            private set

        fun onPriorityChange(priority: Int) {
            this.priority = priority
            onPrioritySave(priority)
        }

        fun onEnabledChange(enabled: Boolean) {
            this.enabled = enabled
            onEnabledSave(enabled)
        }

        fun onSeriesAgeRatingChange(ageRating: Boolean) {
            this.seriesAgeRating = ageRating
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(ageRating = Some(ageRating)))
        }

        fun onSeriesAuthorsChange(value: Boolean) {
            this.seriesAuthors = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(authors = Some(value)))
        }

        fun onSeriesCoverChange(value: Boolean) {
            this.seriesCover = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(thumbnail = Some(value)))
        }

        fun onSeriesGenresChange(value: Boolean) {
            this.seriesGenres = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(genres = Some(value)))
        }

        fun onSeriesLinksChange(value: Boolean) {
            this.seriesLinks = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(links = Some(value)))
        }

        fun onSeriesPublisherChange(value: Boolean) {
            this.seriesPublisher = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(publisher = Some(value)))
        }

        fun onSeriesOriginalPublisherChange(value: Boolean) {
            this.seriesOriginalPublisher = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(useOriginalPublisher = Some(value)))
        }

        fun onSeriesReleaseDateChange(value: Boolean) {
            this.seriesReleaseDate = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(releaseDate = Some(value)))
        }

        fun onSeriesStatusChange(value: Boolean) {
            this.seriesStatus = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(status = Some(value)))
        }

        fun onSeriesSummaryChange(value: Boolean) {
            this.seriesSummary = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(summary = Some(value)))
        }

        fun onSeriesTagsChange(value: Boolean) {
            this.seriesTags = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(tags = Some(value)))
        }

        fun onSeriesTitleChange(value: Boolean) {
            this.seriesTitle = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(title = Some(value)))
        }

        fun onSeriesBookCountChange(value: Boolean) {
            this.seriesBookCount = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(totalBookCount = Some(value)))
        }

        fun onBookEnabledChange(value: Boolean) {
            this.bookEnabled = value
            onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(books = Some(value)))
        }

        fun onBookAuthorsChange(value: Boolean) {
            this.bookAuthors = value
            onBookMetadataSave(BookMetadataConfigUpdateRequest(authors = Some(value)))
        }

        fun onBookCoverChange(value: Boolean) {
            this.bookCover = value
            onBookMetadataSave(BookMetadataConfigUpdateRequest(thumbnail = Some(value)))
        }

        fun onBookIsbnChange(value: Boolean) {
            this.bookIsbn = value
            onBookMetadataSave(BookMetadataConfigUpdateRequest(isbn = Some(value)))
        }

        fun onBookLinksChange(value: Boolean) {
            this.bookLinks = value
            onBookMetadataSave(BookMetadataConfigUpdateRequest(links = Some(value)))
        }

        fun onBookNumberChange(value: Boolean) {
            this.bookNumber = value
            onBookMetadataSave(BookMetadataConfigUpdateRequest(number = Some(value)))
        }

        fun onBookReleaseDateChange(value: Boolean) {
            this.bookReleaseDate = value
            onBookMetadataSave(BookMetadataConfigUpdateRequest(releaseDate = Some(value)))
        }

        fun onBookSummaryChange(value: Boolean) {
            this.bookSummary = value
            onBookMetadataSave(BookMetadataConfigUpdateRequest(summary = Some(value)))
        }

        fun onBookTagsChange(value: Boolean) {
            this.bookTags = value
            onBookMetadataSave(BookMetadataConfigUpdateRequest(tags = Some(value)))
        }

        fun onMediaTypeChange(mediaType: KomfMediaType?) {
            this.mediaType = mediaType
            onMediaTypeSave(mediaType)
        }

        fun onNameMatchingModeChange(nameMatchingMode: KomfNameMatchingMode?) {
            this.nameMatchingMode = nameMatchingMode
            onNameMatchingModeSave(nameMatchingMode)
        }

        fun onAuthorSelect(role: KomfAuthorRole) {
            authorRoles = authorRoles.addOrRemove(role)
            onAuthorRolesSave(authorRoles)
        }

        fun onArtistSelect(role: KomfAuthorRole) {
            artistRoles = artistRoles.addOrRemove(role)
            onArtistRolesSave(authorRoles)
        }

        protected abstract fun onPrioritySave(priority: Int)
        protected abstract fun onEnabledSave(enabled: Boolean)
        protected abstract fun onSeriesMetadataSave(metadata: SeriesMetadataConfigUpdateRequest)
        protected abstract fun onBookMetadataSave(metadata: BookMetadataConfigUpdateRequest)
        protected abstract fun onMediaTypeSave(mediaType: KomfMediaType?)
        protected abstract fun onNameMatchingModeSave(nameMatchingMode: KomfNameMatchingMode?)
        protected abstract fun onAuthorRolesSave(roles: List<KomfAuthorRole>)
        protected abstract fun onArtistRolesSave(roles: List<KomfAuthorRole>)


        private fun <T> List<T>.addOrRemove(value: T): List<T> {
            val mutable = this.toMutableList()
            val existingIndex = mutable.indexOf(value)
            if (existingIndex != -1) mutable.removeAt(existingIndex)
            else mutable.add(value)

            return mutable
        }


        class GenericProviderConfigState(
            provider: KomfProviders,
            config: ProviderConf?,
            private val onMetadataUpdate: (ProviderConfigUpdateRequest, KomfProviders) -> Unit,
        ) : ProviderConfigState(config, provider) {

            override fun onPrioritySave(priority: Int) {
                onMetadataUpdate(ProviderConfigUpdateRequest(priority = Some(priority)))
            }

            override fun onEnabledSave(enabled: Boolean) {
                onMetadataUpdate(ProviderConfigUpdateRequest(enabled = Some(enabled)))
            }

            override fun onSeriesMetadataSave(metadata: SeriesMetadataConfigUpdateRequest) {
                onMetadataUpdate(ProviderConfigUpdateRequest(seriesMetadata = Some(metadata)))
            }

            override fun onBookMetadataSave(metadata: BookMetadataConfigUpdateRequest) {
                onMetadataUpdate(ProviderConfigUpdateRequest(bookMetadata = Some(metadata)))
            }

            override fun onMediaTypeSave(mediaType: KomfMediaType?) {
                onMetadataUpdate(ProviderConfigUpdateRequest(mediaType = mediaType
                    ?.let { Some(it) } ?: PatchValue.None))
            }

            override fun onNameMatchingModeSave(nameMatchingMode: KomfNameMatchingMode?) {
                onMetadataUpdate(ProviderConfigUpdateRequest(nameMatchingMode = nameMatchingMode
                    ?.let { Some(nameMatchingMode) } ?: PatchValue.None
                ))
            }

            override fun onAuthorRolesSave(roles: List<KomfAuthorRole>) {
                onMetadataUpdate(ProviderConfigUpdateRequest(authorRoles = Some(roles)))
            }

            override fun onArtistRolesSave(roles: List<KomfAuthorRole>) {
                onMetadataUpdate(ProviderConfigUpdateRequest(artistRoles = Some(roles)))
            }

            private fun onMetadataUpdate(update: ProviderConfigUpdateRequest) {
                onMetadataUpdate(update, provider)
            }

        }

        class AniListConfigState(
            provider: KomfProviders,
            config: AniListConfigDto?,
            private val onMetadataUpdate: (AniListConfigUpdateRequest) -> Unit,
        ) : ProviderConfigState(config, provider) {

            var tagScoreThreshold by mutableStateOf(config?.tagsScoreThreshold ?: 60)
                private set
            var tagSizeLimit by mutableStateOf(config?.tagsSizeLimit ?: 15)
                private set

            fun onTagScoreThresholdChange(tagThreshold: Int) {
                this.tagScoreThreshold = tagThreshold
                onMetadataUpdate(AniListConfigUpdateRequest(tagsScoreThreshold = Some(tagThreshold)))
            }

            fun onTagSizeLimitChange(sizeLimit: Int) {
                this.tagSizeLimit = sizeLimit
                onMetadataUpdate(AniListConfigUpdateRequest(tagsSizeLimit = Some(tagSizeLimit)))
            }

            override fun onPrioritySave(priority: Int) {
                onMetadataUpdate(AniListConfigUpdateRequest(priority = Some(priority)))
            }

            override fun onEnabledSave(enabled: Boolean) {
                onMetadataUpdate(AniListConfigUpdateRequest(enabled = Some(enabled)))
            }

            override fun onSeriesMetadataSave(metadata: SeriesMetadataConfigUpdateRequest) {
                onMetadataUpdate(AniListConfigUpdateRequest(seriesMetadata = Some(metadata)))
            }

            override fun onBookMetadataSave(metadata: BookMetadataConfigUpdateRequest) {
            }

            override fun onMediaTypeSave(mediaType: KomfMediaType?) {
                onMetadataUpdate(AniListConfigUpdateRequest(mediaType = mediaType
                    ?.let { Some(it) } ?: PatchValue.None))
            }

            override fun onNameMatchingModeSave(nameMatchingMode: KomfNameMatchingMode?) {
                onMetadataUpdate(AniListConfigUpdateRequest(nameMatchingMode = nameMatchingMode
                    ?.let { Some(nameMatchingMode) } ?: PatchValue.None
                ))
            }

            override fun onAuthorRolesSave(roles: List<KomfAuthorRole>) {
                onMetadataUpdate(AniListConfigUpdateRequest(authorRoles = Some(roles)))
            }

            override fun onArtistRolesSave(roles: List<KomfAuthorRole>) {
                onMetadataUpdate(AniListConfigUpdateRequest(artistRoles = Some(roles)))
            }
        }

        class MangaDexConfigState(
            provider: KomfProviders,
            config: MangaDexConfigDto?,
            private val onMetadataUpdate: (MangaDexConfigUpdateRequest) -> Unit,
        ) : ProviderConfigState(config, provider) {

            var coverLanguages by mutableStateOf(config?.coverLanguages ?: listOf("en", "ja"))
            var links by mutableStateOf(config?.links ?: emptyList())

            fun onCoverLanguagesChange(languages: List<String>) {
                this.coverLanguages = languages
                onMetadataUpdate(MangaDexConfigUpdateRequest(coverLanguages = Some(languages)))
            }

            fun onLinkSelect(link: MangaDexLink) {
                links =links.addOrRemove(link)
                onMetadataUpdate(MangaDexConfigUpdateRequest(links = Some(links)))
            }

            override fun onPrioritySave(priority: Int) {
                onMetadataUpdate(MangaDexConfigUpdateRequest(priority = Some(priority)))
            }

            override fun onEnabledSave(enabled: Boolean) {
                onMetadataUpdate(MangaDexConfigUpdateRequest(enabled = Some(enabled)))
            }

            override fun onSeriesMetadataSave(metadata: SeriesMetadataConfigUpdateRequest) {
                onMetadataUpdate(MangaDexConfigUpdateRequest(seriesMetadata = Some(metadata)))
            }

            override fun onBookMetadataSave(metadata: BookMetadataConfigUpdateRequest) {
                onMetadataUpdate(MangaDexConfigUpdateRequest(bookMetadata = Some(metadata)))
            }

            override fun onMediaTypeSave(mediaType: KomfMediaType?) {
                onMetadataUpdate(MangaDexConfigUpdateRequest(mediaType = mediaType
                    ?.let { Some(it) } ?: PatchValue.None))
            }

            override fun onNameMatchingModeSave(nameMatchingMode: KomfNameMatchingMode?) {
                onMetadataUpdate(MangaDexConfigUpdateRequest(nameMatchingMode = nameMatchingMode
                    ?.let { Some(nameMatchingMode) } ?: PatchValue.None
                ))
            }

            override fun onAuthorRolesSave(roles: List<KomfAuthorRole>) {
                onMetadataUpdate(MangaDexConfigUpdateRequest(authorRoles = Some(roles)))
            }

            override fun onArtistRolesSave(roles: List<KomfAuthorRole>) {
                onMetadataUpdate(MangaDexConfigUpdateRequest(artistRoles = Some(roles)))
            }

            private fun <T> List<T>.addOrRemove(value: T): List<T> {
                val mutable = this.toMutableList()
                val existingIndex = mutable.indexOf(value)
                if (existingIndex != -1) mutable.removeAt(existingIndex)
                else mutable.add(value)

                return mutable
            }
        }
    }


}
