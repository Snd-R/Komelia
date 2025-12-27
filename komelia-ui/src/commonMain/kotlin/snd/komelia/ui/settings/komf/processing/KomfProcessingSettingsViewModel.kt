package snd.komelia.ui.settings.komf.processing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.ui.LoadState
import snd.komelia.ui.settings.komf.KomfSharedState
import snd.komf.api.KomfMediaType
import snd.komf.api.KomfReadingDirection
import snd.komf.api.KomfUpdateMode
import snd.komf.api.MediaServer
import snd.komf.api.MediaServer.KAVITA
import snd.komf.api.MediaServer.KOMGA
import snd.komf.api.PatchValue
import snd.komf.api.PatchValue.Some
import snd.komf.api.config.KavitaConfigUpdateRequest
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.config.KomgaConfigUpdateRequest
import snd.komf.api.config.MetadataPostProcessingConfigUpdateRequest
import snd.komf.api.config.MetadataProcessingConfigDto
import snd.komf.api.config.MetadataProcessingConfigUpdateRequest
import snd.komf.api.config.MetadataUpdateConfigUpdateRequest
import snd.komf.api.mediaserver.KomfMediaServerLibraryId
import snd.komf.client.KomfConfigClient

class KomfProcessingSettingsViewModel(
    private val komfConfigClient: KomfConfigClient,
    private val appNotifications: AppNotifications,
    private val serverType: MediaServer,
    val komfSharedState: KomfSharedState,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    val libraries = when (serverType) {
        KOMGA -> komfSharedState.getKomgaLibraries()
        KAVITA -> komfSharedState.getKavitaLibraries()
    }

    val defaultProcessingConfig = MutableStateFlow(
        ProcessingConfigState(
            onMetadataUpdate = this::updateConfig,
            libraryId = null,
            serverType = serverType,
            config = null,
        )
    )

    val libraryProcessingConfigs = MutableStateFlow<Map<KomfMediaServerLibraryId, ProcessingConfigState>>(emptyMap())

    suspend fun initialize() {
        appNotifications.runCatchingToNotifications { komfSharedState.getConfig() }
            .onFailure { mutableState.value = LoadState.Error(it) }
            .onSuccess { config ->
                mutableState.value = LoadState.Success(Unit)
                config.onEach { initFields(it) }.launchIn(screenModelScope)
            }
    }

    private fun initFields(config: KomfConfig) {
        defaultProcessingConfig.value = ProcessingConfigState(
            onMetadataUpdate = this::updateConfig,
            libraryId = null,
            serverType = serverType,
            config = when (serverType) {
                KOMGA -> config.komga.metadataUpdate.default
                KAVITA -> config.kavita.metadataUpdate.default
            }
        )
        libraryProcessingConfigs.value = when (serverType) {
            KOMGA -> config.komga.metadataUpdate.library
            KAVITA -> config.kavita.metadataUpdate.library
        }.map { (libraryId, config) ->
            val komfLibraryId = KomfMediaServerLibraryId(libraryId)
            komfLibraryId to ProcessingConfigState(
                onMetadataUpdate = this::updateConfig,
                libraryId = komfLibraryId,
                serverType = serverType,
                config = config
            )
        }.toMap()
    }

    private fun updateConfig(request: KomfConfigUpdateRequest) {
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications { komfConfigClient.updateConfig(request) }
                .onFailure { initFields(komfSharedState.getConfig().first()) }
        }
    }

    fun onNewLibraryTabAdd(libraryId: KomfMediaServerLibraryId) {
        libraryProcessingConfigs.update {
            it.plus(
                libraryId to ProcessingConfigState(
                    onMetadataUpdate = this::updateConfig,
                    libraryId = libraryId,
                    serverType = serverType,
                    config = null
                )
            )
        }

        val metadataUpdate = MetadataUpdateConfigUpdateRequest(
            library = Some(mapOf(libraryId.value to MetadataProcessingConfigUpdateRequest()))
        )
        val request = toKomfConfigUpdateRequest(metadataUpdate, serverType)
        updateConfig(request)
    }

    fun onLibraryTabRemove(libraryId: KomfMediaServerLibraryId) {
        libraryProcessingConfigs.update { it.minus(libraryId) }

        val metadataUpdate = MetadataUpdateConfigUpdateRequest(
            library = Some(mapOf(libraryId.value to null))
        )
        val request = toKomfConfigUpdateRequest(metadataUpdate, serverType)
        updateConfig(request)
    }


    class ProcessingConfigState(
        private val onMetadataUpdate: (KomfConfigUpdateRequest) -> Unit,
        private val libraryId: KomfMediaServerLibraryId?,
        private val serverType: MediaServer,
        config: MetadataProcessingConfigDto?,
    ) {
        var libraryType by mutableStateOf(config?.libraryType ?: KomfMediaType.MANGA)
            private set
        var aggregate by mutableStateOf(config?.aggregate ?: false)
            private set
        var mergeTags by mutableStateOf(config?.mergeTags ?: false)
            private set
        var mergeGenres by mutableStateOf(config?.mergeGenres ?: false)
            private set
        var bookCovers by mutableStateOf(config?.bookCovers ?: false)
            private set
        var seriesCovers by mutableStateOf(config?.seriesCovers ?: false)
            private set
        var overrideExistingCovers by mutableStateOf(config?.overrideExistingCovers ?: false)
            private set
        var updateModes by mutableStateOf(config?.updateModes ?: listOf(KomfUpdateMode.API))
            private set

        var seriesTitle by mutableStateOf(config?.postProcessing?.seriesTitle ?: false)
            private set
        var alternativeSeriesTitles by mutableStateOf(config?.postProcessing?.alternativeSeriesTitles ?: false)
            private set
        var seriesTitleLanguage by mutableStateOf(config?.postProcessing?.seriesTitleLanguage ?: "")
            private set

        var alternativeSeriesTitleLanguages by mutableStateOf(
            config?.postProcessing?.alternativeSeriesTitleLanguages ?: emptyList()
        )
            private set
        var fallbackToAltTitle by mutableStateOf(config?.postProcessing?.fallbackToAltTitle ?: false)
            private set
        var orderBooks by mutableStateOf(config?.postProcessing?.orderBooks ?: false)
            private set

        var readingDirectionValue by mutableStateOf(config?.postProcessing?.readingDirectionValue)
            private set
        var defaultLanguageValue by mutableStateOf(config?.postProcessing?.languageValue)
            private set

        fun onLibraryTypeChange(libraryType: KomfMediaType) {
            this.libraryType = libraryType
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(libraryType = Some(libraryType)))
        }

        fun onAggregateChange(aggregate: Boolean) {
            this.aggregate = aggregate
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(aggregate = Some(aggregate)))
        }

        fun onMergeTagsChange(mergeTags: Boolean) {
            this.mergeTags = mergeTags
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(mergeTags = Some(mergeTags)))
        }

        fun onMergeGenresChange(mergeGenres: Boolean) {
            this.mergeGenres = mergeGenres
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(mergeGenres = Some(mergeGenres)))
        }

        fun onBookCoversChange(bookCovers: Boolean) {
            this.bookCovers = bookCovers
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(bookCovers = Some(bookCovers)))
        }

        fun onSeriesCoversChange(seriesCovers: Boolean) {
            this.seriesCovers = seriesCovers
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(seriesCovers = Some(seriesCovers)))
        }

        fun onOverrideExistingCoversChange(overrideExistingCovers: Boolean) {
            this.overrideExistingCovers = overrideExistingCovers
            onMetadataUpdate(
                MetadataProcessingConfigUpdateRequest(
                    overrideExistingCovers = Some(overrideExistingCovers)
                )
            )
        }

        fun onUpdateModeSelect(updateMode: KomfUpdateMode) {
            this.updateModes = updateModes.addOrRemove(updateMode)
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(updateModes = Some(updateModes)))
        }

        fun onSeriesTitleChange(seriesTitle: Boolean) {
            this.seriesTitle = seriesTitle
            val postProcessingUpdate = MetadataPostProcessingConfigUpdateRequest(seriesTitle = Some(seriesTitle))
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(postProcessing = Some(postProcessingUpdate)))
        }

        fun onAlternativeSeriesTitlesChange(altTitles: Boolean) {
            this.alternativeSeriesTitles = altTitles
            val postProcessingUpdate =
                MetadataPostProcessingConfigUpdateRequest(alternativeSeriesTitles = Some(altTitles))
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(postProcessing = Some(postProcessingUpdate)))
        }

        fun onSeriesTitleLanguageChange(seriesTitleLanguage: String) {
            this.seriesTitleLanguage = seriesTitleLanguage
        }

        fun onSeriesTitleLanguageSave() {
            val postProcessingUpdate =
                MetadataPostProcessingConfigUpdateRequest(seriesTitleLanguage = Some(seriesTitleLanguage))
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(postProcessing = Some(postProcessingUpdate)))
        }

        fun onAlternativeTitleLanguagesChange(alternativeSeriesTitleLanguages: List<String>) {
            this.alternativeSeriesTitleLanguages = alternativeSeriesTitleLanguages
            val postProcessingUpdate = MetadataPostProcessingConfigUpdateRequest(
                alternativeSeriesTitleLanguages = Some(alternativeSeriesTitleLanguages)
            )
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(postProcessing = Some(postProcessingUpdate)))
        }

        fun onFallbackToAltTitleChange(fallbackToAltTitle: Boolean) {
            this.fallbackToAltTitle = fallbackToAltTitle
            val postProcessingUpdate =
                MetadataPostProcessingConfigUpdateRequest(fallbackToAltTitle = Some(fallbackToAltTitle))
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(postProcessing = Some(postProcessingUpdate)))
        }

        fun onOrderBooksChange(orderBooks: Boolean) {
            this.orderBooks = orderBooks
            val postProcessingUpdate = MetadataPostProcessingConfigUpdateRequest(orderBooks = Some(orderBooks))
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(postProcessing = Some(postProcessingUpdate)))
        }

        fun onReadingDirectionChange(readingDirection: KomfReadingDirection?) {
            this.readingDirectionValue = readingDirection
            val postProcessingUpdate = MetadataPostProcessingConfigUpdateRequest(
                readingDirectionValue = readingDirection?.let { Some(readingDirection) } ?: PatchValue.None
            )
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(postProcessing = Some(postProcessingUpdate)))
        }

        fun onDefaultLanguageChange(language: String) {
            this.defaultLanguageValue = language
        }

        fun onDefaultLanguageSave() {
            val postProcessingUpdate = MetadataPostProcessingConfigUpdateRequest(
                languageValue = defaultLanguageValue?.let {
                    if (it.isBlank()) PatchValue.None
                    else Some(it)
                } ?: PatchValue.None
            )
            onMetadataUpdate(MetadataProcessingConfigUpdateRequest(postProcessing = Some(postProcessingUpdate)))
        }

        private fun onMetadataUpdate(update: MetadataProcessingConfigUpdateRequest) {
            val metadataUpdateRequest = if (libraryId != null) {
                MetadataUpdateConfigUpdateRequest(library = Some(mapOf(libraryId.value to update)))
            } else {
                MetadataUpdateConfigUpdateRequest(default = Some(update))
            }

            val request = toKomfConfigUpdateRequest(metadataUpdateRequest, serverType)
            onMetadataUpdate(request)
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

private fun toKomfConfigUpdateRequest(
    request: MetadataUpdateConfigUpdateRequest,
    serverType: MediaServer
): KomfConfigUpdateRequest {
    return when (serverType) {
        KOMGA -> KomfConfigUpdateRequest(
            komga = Some(
                KomgaConfigUpdateRequest(metadataUpdate = Some(request))
            )
        )

        KAVITA -> KomfConfigUpdateRequest(
            kavita = Some(
                KavitaConfigUpdateRequest(metadataUpdate = Some(request))
            )
        )
    }
}
