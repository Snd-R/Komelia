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
            label = { Text("Update Modes") },
            placeholder = "None",
            inputFieldModifier = Modifier.fillMaxWidth()
        )

        DropdownChoiceMenu(
            selectedOption = LabeledEntry(state.libraryType, state.libraryType.name),
            options = remember { KomfMediaType.entries.map { LabeledEntry(it, it.name) } },
            onOptionChange = { state.onLibraryTypeChange(it.value) },
            label = { Text("Library type. Affects some options, mainly book name parsing") },
            inputFieldModifier = Modifier.fillMaxWidth(),
        )

        SwitchWithLabel(
            checked = state.orderBooks,
            onCheckedChange = state::onOrderBooksChange,
            label = { Text("Order Books") },

            supportingText = {
                Text(
                    "Attempt to order books using naming pattern based on selected library type",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        HorizontalDivider()

        Text("Aggregation settings", style = MaterialTheme.typography.titleLarge)
        SwitchWithLabel(
            checked = state.aggregate,
            onCheckedChange = state::onAggregateChange,
            label = { Text("Aggregate") },
            supportingText = {
                Text(
                    "aggregate and combine metadata from all enabled providers instead of taking first matched result",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.mergeGenres,
            onCheckedChange = state::onMergeGenresChange,
            enabled = state.aggregate,
            label = { Text("Merge Genres") },
            supportingText = {
                Text(
                    "if aggregate option is enabled merge genres instead of taking them from first matched result",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.mergeTags,
            onCheckedChange = state::onMergeTagsChange,
            enabled = state.aggregate,
            label = { Text("Merge Tags") },

            supportingText = {
                Text(
                    "if aggregate option is enabled merge tags instead of taking them from first matched result",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        HorizontalDivider()
        Text("Cover settings", style = MaterialTheme.typography.titleLarge)
        SwitchWithLabel(
            checked = state.seriesCovers,
            onCheckedChange = state::onSeriesCoversChange,
            label = { Text("Series Covers") },

            supportingText = {
                Text(
                    "Upload series covers",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.bookCovers,
            onCheckedChange = state::onBookCoversChange,
            label = { Text("Book Covers") },

            supportingText = {
                Text(
                    "Upload book covers",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.overrideExistingCovers,
            onCheckedChange = state::onOverrideExistingCoversChange,
            label = { Text("Override Existing Covers") },

            supportingText = {
                Text(
                    "If entry already has a user uploaded cover, mark newly uploaded cover as current.\nIf disabled, then upload cover without selecting it",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        HorizontalDivider()
        Text("Title Settings", style = MaterialTheme.typography.titleLarge)
        SwitchWithLabel(
            checked = state.seriesTitle,
            onCheckedChange = state::onSeriesTitleChange,
            label = { Text("Series Title") },

            supportingText = {
                Text(
                    "Update series title if matched metadata contains title data",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        SwitchWithLabel(
            checked = state.alternativeSeriesTitles,
            onCheckedChange = state::onAlternativeSeriesTitlesChange,
            label = { Text("Alternative Series Titles") },

            supportingText = {
                Text(
                    "Update series alternative title if matched metadata contains alternative title data",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        SwitchWithLabel(
            checked = state.fallbackToAltTitle,
            onCheckedChange = state::onFallbackToAltTitleChange,
            label = { Text("Alternative Title Fallback") },

            supportingText = {
                Text(
                    "Use first available alternative title if no main title with specified language is found",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        LanguageSelectionField(
            label = "Series title language (ISO 639)",
            languageValue = state.seriesTitleLanguage,
            onLanguageValueChange = state::onSeriesTitleLanguageChange,
            onLanguageValueSave = state::onSeriesTitleLanguageSave
        )
        ChipFieldWithSuggestions(
            label = { Text("Alternative title languages (ISO 639)") },
            values = state.alternativeSeriesTitleLanguages,
            onValuesChange = state::onAlternativeTitleLanguagesChange,
            suggestions = komfLanguageTagsSuggestions
        )
        HorizontalDivider()
        Text("Default values", style = MaterialTheme.typography.titleLarge)
        if (serverType == KOMGA) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(state.readingDirectionValue, state.readingDirectionValue?.name ?: "None"),
                options = remember {
                    listOf(LabeledEntry<KomfReadingDirection?>(null, "None")) +
                            KomfReadingDirection.entries.map { LabeledEntry(it, it.name) }
                },
                onOptionChange = { state.onReadingDirectionChange(it.value) },
                label = { Text("Default series reading direction") },
                inputFieldModifier = Modifier.fillMaxWidth(),
            )
        }
        LanguageSelectionField(
            label = "Default series language",
            languageValue = state.defaultLanguageValue ?: "",
            onLanguageValueChange = state::onDefaultLanguageChange,
            onLanguageValueSave = state::onDefaultLanguageSave
        )

        Spacer(Modifier.height(30.dp))
    }
}

