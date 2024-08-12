package io.github.snd_r.komelia.ui.settings.komf.processing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.settings.komf.KomfConfigState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komf.api.KomfMediaType
import snd.komf.api.KomfReadingDirection
import snd.komf.api.KomfUpdateMode
import snd.komf.api.PatchValue
import snd.komf.api.PatchValue.Some
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.config.KomgaConfigUpdateRequest
import snd.komf.api.config.MetadataPostProcessingConfigUpdateRequest
import snd.komf.api.config.MetadataProcessingConfigDto
import snd.komf.api.config.MetadataProcessingConfigUpdateRequest
import snd.komf.api.config.MetadataUpdateConfigUpdateRequest
import snd.komf.client.KomfConfigClient
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId

class KomfProcessingSettingsViewModel(
    private val komfConfigClient: KomfConfigClient,
    private val appNotifications: AppNotifications,
    val komfConfig: KomfConfigState,
    val libraries: StateFlow<List<KomgaLibrary>>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    var defaultProcessingConfig by mutableStateOf(ProcessingConfigState(this::updateConfig, null, null))
        private set
    var libraryProcessingConfigs by mutableStateOf<Map<KomgaLibraryId, ProcessingConfigState>>(emptyMap())
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
        defaultProcessingConfig = ProcessingConfigState(this::updateConfig, null, config.komga.metadataUpdate.default)
        libraryProcessingConfigs = config.komga.metadataUpdate.library
            .map { (libraryId, config) ->
                val komgaLibraryId = KomgaLibraryId(libraryId)
                komgaLibraryId to ProcessingConfigState(this::updateConfig, komgaLibraryId, config)
            }.toMap()
    }

    private fun updateConfig(request: KomfConfigUpdateRequest) {
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications { komfConfigClient.updateConfig(request) }
                .onFailure { mutableState.value = LoadState.Error(it) }
        }
    }

    fun onNewLibraryTabAdd(libraryId: KomgaLibraryId) {
        libraryProcessingConfigs = libraryProcessingConfigs.plus(
            libraryId to ProcessingConfigState(this::updateConfig, libraryId, null)
        )

        val metadataUpdate = MetadataUpdateConfigUpdateRequest(
            library = Some(mapOf(libraryId.value to MetadataProcessingConfigUpdateRequest()))
        )
        val komgaUpdate = KomgaConfigUpdateRequest(metadataUpdate = Some(metadataUpdate))
        updateConfig(KomfConfigUpdateRequest(komga = Some(komgaUpdate)))
    }

    fun onLibraryTabRemove(libraryId: KomgaLibraryId) {
        libraryProcessingConfigs = libraryProcessingConfigs.minus(libraryId)

        val metadataUpdate = MetadataUpdateConfigUpdateRequest(
            library = Some(mapOf(libraryId.value to null))
        )
        val komgaUpdate = KomgaConfigUpdateRequest(metadataUpdate = Some(metadataUpdate))
        updateConfig(KomfConfigUpdateRequest(komga = Some(komgaUpdate)))
    }


    class ProcessingConfigState(
        private val onMetadataUpdate: (KomfConfigUpdateRequest) -> Unit,
        private val libraryId: KomgaLibraryId?,
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

            val komgaUpdate = KomgaConfigUpdateRequest(metadataUpdate = Some(metadataUpdateRequest))
            onMetadataUpdate(KomfConfigUpdateRequest(komga = Some(komgaUpdate)))
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
