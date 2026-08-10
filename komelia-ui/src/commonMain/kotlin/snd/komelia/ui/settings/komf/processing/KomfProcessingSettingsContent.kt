package snd.komelia.ui.settings.komf.processing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_aggreation_settings
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_aggregate
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_aggregate_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_alt_title_fallback
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_alt_title_fallback_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_book_covers
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_book_covers_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_cover_settings
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_default_language
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_default_reading_direction
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_default_values
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_library_type
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_merge_genres
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_merge_genres_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_merge_tags
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_merge_tags_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_none
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_order_books
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_order_books_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_override_covers
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_override_covers_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_series_alt_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_series_alt_title_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_series_alt_title_language
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_series_covers
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_series_covers_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_series_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_series_title_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_series_title_language
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_title_settings
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_update_modes
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.common.components.ChipFieldWithSuggestions
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.DropdownMultiChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.SwitchWithLabel
import snd.komelia.ui.settings.komf.LanguageSelectionField
import snd.komelia.ui.settings.komf.LibraryTabs
import snd.komelia.ui.settings.komf.komfLanguageTagsSuggestions
import snd.komelia.ui.settings.komf.processing.KomfProcessingSettingsViewModel.ProcessingConfigState
import snd.komf.api.KomfMediaType
import snd.komf.api.KomfReadingDirection
import snd.komf.api.KomfUpdateMode
import snd.komf.api.MediaServer
import snd.komf.api.MediaServer.KOMGA
import snd.komf.api.mediaserver.KomfMediaServerLibrary
import snd.komf.api.mediaserver.KomfMediaServerLibraryId

@Composable
fun KomfProcessingSettingsContent(
    defaultProcessingState: ProcessingConfigState,
    libraryProcessingState: Map<KomfMediaServerLibraryId, ProcessingConfigState>,

    onLibraryConfigAdd: (libraryId: KomfMediaServerLibraryId) -> Unit,
    onLibraryConfigRemove: (libraryId: KomfMediaServerLibraryId) -> Unit,
    libraries: List<KomfMediaServerLibrary>,
    serverType: MediaServer,
) {
    LibraryTabs(
        defaultProcessingState,
        libraryProcessingState,
        onLibraryConfigAdd, onLibraryConfigRemove, libraries
    ) {

        ProcessingConfigContent(it, serverType)
    }
}

@Composable
private fun ProcessingConfigContent(
    state: ProcessingConfigState,
    serverType: MediaServer,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DropdownMultiChoiceMenu(
            selectedOptions = state.updateModes.map { LabeledEntry(it, it.name) },
            options = remember { KomfUpdateMode.entries.map { LabeledEntry(it, it.name) } },
            onOptionSelect = { state.onUpdateModeSelect(it.value) },
            label = { Text(stringResource(Res.string.komf_processing_update_modes)) },
            placeholder = stringResource(Res.string.komf_processing_none),
            inputFieldModifier = Modifier.fillMaxWidth()
        )

        DropdownChoiceMenu(
            selectedOption = LabeledEntry(state.libraryType, state.libraryType.name),
            options = remember { KomfMediaType.entries.map { LabeledEntry(it, it.name) } },
            onOptionChange = { state.onLibraryTypeChange(it.value) },
            label = { Text(stringResource(Res.string.komf_processing_library_type)) },
            inputFieldModifier = Modifier.fillMaxWidth(),
        )

        SwitchWithLabel(
            checked = state.orderBooks,
            onCheckedChange = state::onOrderBooksChange,
            label = { Text(stringResource(Res.string.komf_processing_order_books)) },

            supportingText = {
                Text(
                    stringResource(Res.string.komf_processing_order_books_desc),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        HorizontalDivider()

        Text(
            stringResource(Res.string.komf_processing_aggreation_settings),
            style = MaterialTheme.typography.titleLarge
        )
        SwitchWithLabel(
            checked = state.aggregate,
            onCheckedChange = state::onAggregateChange,
            label = { Text(stringResource(Res.string.komf_processing_aggregate)) },
            supportingText = {
                Text(
                    stringResource(Res.string.komf_processing_aggregate_desc),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.mergeGenres,
            onCheckedChange = state::onMergeGenresChange,
            enabled = state.aggregate,
            label = { Text(stringResource(Res.string.komf_processing_merge_genres)) },
            supportingText = {
                Text(
                    stringResource(Res.string.komf_processing_merge_genres_desc),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.mergeTags,
            onCheckedChange = state::onMergeTagsChange,
            enabled = state.aggregate,
            label = { Text(stringResource(Res.string.komf_processing_merge_tags)) },

            supportingText = {
                Text(
                    stringResource(Res.string.komf_processing_merge_tags_desc),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        HorizontalDivider()
        Text(
            stringResource(Res.string.komf_processing_cover_settings),
            style = MaterialTheme.typography.titleLarge
        )
        SwitchWithLabel(
            checked = state.seriesCovers,
            onCheckedChange = state::onSeriesCoversChange,
            label = { Text(stringResource(Res.string.komf_processing_series_covers)) },

            supportingText = {
                Text(
                    stringResource(Res.string.komf_processing_series_covers_desc),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.bookCovers,
            onCheckedChange = state::onBookCoversChange,
            label = { Text(stringResource(Res.string.komf_processing_book_covers)) },

            supportingText = {
                Text(
                    stringResource(Res.string.komf_processing_book_covers_desc),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.overrideExistingCovers,
            onCheckedChange = state::onOverrideExistingCoversChange,
            label = { Text(stringResource(Res.string.komf_processing_override_covers)) },

            supportingText = {
                Text(
                    stringResource(Res.string.komf_processing_override_covers_desc),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        HorizontalDivider()
        Text(
            stringResource(Res.string.komf_processing_title_settings),
            style = MaterialTheme.typography.titleLarge
        )
        SwitchWithLabel(
            checked = state.seriesTitle,
            onCheckedChange = state::onSeriesTitleChange,
            label = { Text(stringResource(Res.string.komf_processing_series_title)) },

            supportingText = {
                Text(
                    stringResource(Res.string.komf_processing_series_title_desc),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        SwitchWithLabel(
            checked = state.alternativeSeriesTitles,
            onCheckedChange = state::onAlternativeSeriesTitlesChange,
            label = { Text(stringResource(Res.string.komf_processing_series_alt_title)) },

            supportingText = {
                Text(
                    stringResource(Res.string.komf_processing_series_alt_title_desc),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        SwitchWithLabel(
            checked = state.fallbackToAltTitle,
            onCheckedChange = state::onFallbackToAltTitleChange,
            label = { Text(stringResource(Res.string.komf_processing_alt_title_fallback)) },

            supportingText = {
                Text(
                    stringResource(Res.string.komf_processing_alt_title_fallback_desc),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        LanguageSelectionField(
            label = stringResource(Res.string.komf_processing_series_title_language),
            languageValue = state.seriesTitleLanguage,
            onLanguageValueChange = state::onSeriesTitleLanguageChange,
            onLanguageValueSave = state::onSeriesTitleLanguageSave
        )
        ChipFieldWithSuggestions(
            label = { Text(stringResource(Res.string.komf_processing_series_alt_title_language)) },
            values = state.alternativeSeriesTitleLanguages,
            onValuesChange = state::onAlternativeTitleLanguagesChange,
            suggestions = komfLanguageTagsSuggestions
        )
        HorizontalDivider()
        Text(
            stringResource(Res.string.komf_processing_default_values),
            style = MaterialTheme.typography.titleLarge
        )
        val noneString = stringResource(Res.string.komf_processing_none)
        if (serverType == KOMGA) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(state.readingDirectionValue, state.readingDirectionValue?.name ?: "None"),
                options = remember {
                    listOf(LabeledEntry<KomfReadingDirection?>(null, noneString)) +
                            KomfReadingDirection.entries.map { LabeledEntry(it, it.name) }
                },
                onOptionChange = { state.onReadingDirectionChange(it.value) },
                label = { Text(stringResource(Res.string.komf_processing_default_reading_direction)) },
                inputFieldModifier = Modifier.fillMaxWidth(),
            )
        }
        LanguageSelectionField(
            label = stringResource(Res.string.komf_processing_default_language),
            languageValue = state.defaultLanguageValue ?: "",
            onLanguageValueChange = state::onDefaultLanguageChange,
            onLanguageValueSave = state::onDefaultLanguageSave
        )

        Spacer(Modifier.height(30.dp))
    }
}
