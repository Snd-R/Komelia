package snd.komelia.ui.settings.komf.providers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_edit_tab_book_metadata
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_edit_tab_provider_settings
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_edit_tab_series_metadata
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_add
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_anilist_tag_score_threshold
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_artist_roles
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_author_roles
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_book_authors
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_book_cover
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_book_enabled
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_book_isbn
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_book_links
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_book_number
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_book_release_date
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_book_summary
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_book_tags
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_comicvine_client_id
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_edit
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_edit_close
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mal_client_id
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database_checksum
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database_download
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database_download_close
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database_download_date
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database_download_done
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_database_update
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangabaka_datasource_type
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangadex_alt_title_languages
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangadex_inlcude_links
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_mangadex_inlcude_links_all
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_media_type
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_name_matching_mode
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_age_rating
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_authors
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_book_count
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_cover
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_genres
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_links
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_publisher
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_publisher_original
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_publisher_original_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_release_date
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_status
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_summary
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_tags
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_series_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers_unset
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableColumn
import snd.komelia.DefaultDateTimeFormats.localDateFormat
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.common.components.ChipFieldWithSuggestions
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.DropdownMultiChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.SwitchWithLabel
import snd.komelia.ui.common.components.UpdateProgressContent
import snd.komelia.ui.common.components.scrollbar
import snd.komelia.ui.dialogs.AppDialog
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabDialog
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komelia.ui.platform.WindowSizeClass
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.platform.cursorForMove
import snd.komelia.ui.settings.komf.LibraryTabs
import snd.komelia.ui.settings.komf.SavableTextField
import snd.komelia.ui.settings.komf.komfLanguageTagsSuggestions
import snd.komelia.ui.settings.komf.providers.KomfProvidersSettingsViewModel.ProvidersConfigState
import snd.komelia.ui.strings.AppStrings
import snd.komelia.updates.UpdateProgress
import snd.komf.api.KomfAuthorRole
import snd.komf.api.KomfCoreProviders
import snd.komf.api.KomfMediaType
import snd.komf.api.KomfNameMatchingMode
import snd.komf.api.KomfProviders
import snd.komf.api.MangaBakaMode
import snd.komf.api.MangaDexLink
import snd.komf.api.config.MangaBakaDatabaseDto
import snd.komf.api.config.MangaBakaDownloadProgress
import snd.komf.api.mediaserver.KomfMediaServerLibrary
import snd.komf.api.mediaserver.KomfMediaServerLibraryId

@Composable
fun KomfProvidersSettingsContent(
    defaultProcessingState: ProvidersConfigState,
    libraryProcessingState: Map<KomfMediaServerLibraryId, ProvidersConfigState>,

    onLibraryConfigAdd: (libraryId: KomfMediaServerLibraryId) -> Unit,
    onLibraryConfigRemove: (libraryId: KomfMediaServerLibraryId) -> Unit,
    libraries: List<KomfMediaServerLibrary>,

    nameMatchingMode: KomfNameMatchingMode,
    onNameMatchingModeChange: (KomfNameMatchingMode) -> Unit,

    comicVineClientId: String?,
    onComicVineClientIdSave: (String) -> Unit,

    malClientId: String?,
    onMalClientIdSave: (String) -> Unit,

    mangaBakaDbMetadata: MangaBakaDatabaseDto?,
    onMangaBakaUpdate: () -> Flow<MangaBakaDownloadProgress>
) {

    LibraryTabs(
        defaultProcessingState = defaultProcessingState,
        libraryProcessingState = libraryProcessingState,
        onLibraryConfigAdd = onLibraryConfigAdd,
        onLibraryConfigRemove = onLibraryConfigRemove,
        libraries = libraries
    ) { state ->
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ProvidersConfigContent(state, state::onProviderReorder)

            if (state == defaultProcessingState) {
                HorizontalDivider(Modifier.padding(vertical = 20.dp))
                CommonSettingsContent(
                    nameMatchingMode,
                    onNameMatchingModeChange = onNameMatchingModeChange,
                    comicVineClientId = comicVineClientId,
                    onComicVineClientIdSave = onComicVineClientIdSave,
                    malClientId = malClientId,
                    onMalClientIdSave = onMalClientIdSave,
                    mangaBakaDbMetadata = mangaBakaDbMetadata,
                    onMangaBakaUpdate = onMangaBakaUpdate
                )

            }
        }

    }
}

@Composable
private fun ProvidersConfigContent(
    state: ProvidersConfigState,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ReorderableColumn(
            list = state.enabledProviders,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            onSettle = onReorder,
        ) { _, item, isDragging ->
            key(item) {

                ReorderableItem {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .height(70.dp)
                            .fillMaxWidth()
                            .background(
                                if (isDragging) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 5.dp)
                                .widthIn(40.dp)
                                .draggableHandle()
                                .cursorForMove()
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.DragHandle,
                                contentDescription = null,
                            )

                        }

                        ProviderCard(item, state::onProviderRemove)

                    }
                }
            }
        }
    }

    AddNewProviderButton(
        onNewProviderAdd = state::onProviderAdd,
        enabledProviders = remember(state.enabledProviders) { state.enabledProviders.map { it.provider } },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNewProviderButton(
    onNewProviderAdd: (KomfProviders) -> Unit,
    enabledProviders: List<KomfProviders>,
) {
    var addProviderExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = addProviderExpanded,
        onExpandedChange = { addProviderExpanded = it },
    ) {
        FilledTonalButton(
            onClick = { addProviderExpanded = true },
            modifier = Modifier
                .cursorForHand()
                .menuAnchor(PrimaryNotEditable)
        ) {
            Text(stringResource(Res.string.komf_providers_add))
        }

        val scrollState = rememberScrollState()
        ExposedDropdownMenu(
            expanded = addProviderExpanded,
            onDismissRequest = { addProviderExpanded = false },
            scrollState = scrollState,
            modifier = Modifier
                .widthIn(min = 200.dp)
                .scrollbar(scrollState, Orientation.Vertical)
        ) {
            KomfCoreProviders.entries.filter { it !in enabledProviders }.forEach {
                DropdownMenuItem(
                    text = { Text(AppStrings.forProvider(it)) },
                    onClick = {
                        addProviderExpanded = false
                        onNewProviderAdd(it)
                    },
                    modifier = Modifier.cursorForHand()
                )

            }
        }
    }

}

@Composable
private fun CommonSettingsContent(
    nameMatchingMode: KomfNameMatchingMode,
    onNameMatchingModeChange: (KomfNameMatchingMode) -> Unit,

    comicVineClientId: String?,
    onComicVineClientIdSave: (String) -> Unit,

    malClientId: String?,
    onMalClientIdSave: (String) -> Unit,
    mangaBakaDbMetadata: MangaBakaDatabaseDto?,
    onMangaBakaUpdate: () -> Flow<MangaBakaDownloadProgress>
) {
    var showMangaBakaDownloadProgress by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DropdownChoiceMenu(
            selectedOption = remember(nameMatchingMode) {
                LabeledEntry(
                    nameMatchingMode,
                    nameMatchingMode.name
                )
            },
            options = remember { KomfNameMatchingMode.entries.map { LabeledEntry(it, it.name) } },
            onOptionChange = { onNameMatchingModeChange(it.value) },
            label = { Text(stringResource(Res.string.komf_providers_name_matching_mode)) },
            inputFieldModifier = Modifier.fillMaxWidth()
        )

        SavableTextField(
            currentValue = comicVineClientId ?: "",
            onValueSave = onComicVineClientIdSave,
            useEditButton = true,
            label = { Text(stringResource(Res.string.komf_providers_comicvine_client_id)) }
        )
        SavableTextField(
            currentValue = malClientId ?: "",
            onValueSave = onMalClientIdSave,
            useEditButton = true,
            label = { Text(stringResource(Res.string.komf_providers_mal_client_id)) }
        )

        HorizontalDivider()
        Text(
            stringResource(Res.string.komf_providers_mangabaka_database),
            style = MaterialTheme.typography.titleLarge
        )
        Column {
            if (mangaBakaDbMetadata != null) {
                val downloadDate = remember(mangaBakaDbMetadata) {
                    mangaBakaDbMetadata.downloadTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(localDateFormat)
                }
                Text(stringResource(Res.string.komf_providers_mangabaka_database_download_date, downloadDate))
                Text(
                    stringResource(
                        Res.string.komf_providers_mangabaka_database_checksum, mangaBakaDbMetadata.checksum
                    )
                )
            }
            FilledTonalButton(
                onClick = { showMangaBakaDownloadProgress = true },
                modifier = Modifier.cursorForHand()
            ) {
                Text(
                    if (mangaBakaDbMetadata != null) stringResource(Res.string.komf_providers_mangabaka_database_update)
                    else stringResource(Res.string.komf_providers_mangabaka_database_update)
                )
            }
        }
        if (showMangaBakaDownloadProgress) {
            MangaBakaDbDownloadContent(
                onMangaBakaUpdate,
                { showMangaBakaDownloadProgress = false })
        }
    }
}

@Composable
private fun MangaBakaDbDownloadContent(
    onDownloadRequest: () -> Flow<MangaBakaDownloadProgress>,
    onDismiss: () -> Unit,
) {
    var progress by remember { mutableStateOf(UpdateProgress(0, 0)) }
    var error by remember { mutableStateOf<String?>(null) }
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onDownloadRequest().collect { event ->
            when (event) {
                is MangaBakaDownloadProgress.ProgressEvent -> progress = UpdateProgress(
                    event.total,
                    event.completed,
                    event.info
                )

                is MangaBakaDownloadProgress.ErrorEvent -> {
                    error = event.message
                    completed = true
                }

                MangaBakaDownloadProgress.FinishedEvent -> completed = true
            }
        }
    }

    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        header = {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    stringResource(Res.string.komf_providers_mangabaka_database_download),
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider(Modifier.padding(top = 10.dp))
            }
        },
        content = {
            val errorText = error
            when {
                errorText != null -> Text(errorText, Modifier.padding(20.dp))
                completed -> Text(
                    stringResource(Res.string.komf_providers_mangabaka_database_download_done),
                    Modifier.padding(20.dp)
                )

                else -> UpdateProgressContent(
                    progress.total,
                    progress.completed,
                    progress.description
                )
            }
        },
        controlButtons = {
            Box(modifier = Modifier.padding(bottom = 10.dp, end = 10.dp)) {
                if (completed) {
                    FilledTonalButton(
                        onClick = onDismiss,
                        modifier = Modifier.cursorForHand(),
                        content = {
                            Text(stringResource(Res.string.komf_providers_mangabaka_database_download_close))
                        }
                    )

                } else {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.cursorForHand(),
                        content = {
                            Text(stringResource(Res.string.komf_providers_mangabaka_database_download_close))
                        }
                    )
                }
            }
        },
        onDismissRequest = onDismiss
    )
}

@Composable
private fun ProviderCard(
    state: ProviderConfigState,
    onProviderRemove: (ProviderConfigState) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "${state.priority}. ${AppStrings.forProvider(state.provider)}",
        )

        IconButton(
            onClick = { showEditDialog = true },
            modifier = Modifier.cursorForHand()
        ) {
            Icon(Icons.Default.Edit, null)
        }
        Spacer(Modifier.weight(1.0f))
        IconButton(
            onClick = { onProviderRemove(state) },
            modifier = Modifier.cursorForHand()
        ) {
            Icon(Icons.Default.Delete, null)
        }
    }

    val tabs = remember(state) {
        listOfNotNull(
            SeriesMetadataTab(state),
            if (state.isBookMetadataAvailable) BookMetadataTab(state) else null,
            ProviderSettingsTab(state)
        )
    }
    var currentTab by remember { mutableStateOf(tabs.first()) }
    if (showEditDialog) {

        TabDialog(
            modifier = when (LocalWindowWidth.current) {
                WindowSizeClass.COMPACT, WindowSizeClass.MEDIUM -> Modifier
                else -> Modifier.widthIn(max = 700.dp)
            },
            title = stringResource(Res.string.komf_providers_edit, AppStrings.forProvider(state.provider)),
            currentTab = currentTab,
            tabs = tabs,
            onTabChange = { currentTab = it },
            showCancelButton = false,
            onConfirm = { showEditDialog = false },
            confirmationText = stringResource(Res.string.komf_providers_edit_close),
            onDismissRequest = { showEditDialog = false },
        )
    }
}

private class SeriesMetadataTab(private val state: ProviderConfigState) : DialogTab {
    override fun options() = TabItem(title = Res.string.komf_edit_tab_series_metadata)

    @Composable
    override fun Content() {
        Column {
            SwitchWithLabel(
                checked = state.seriesAgeRating,
                onCheckedChange = state::onSeriesAgeRatingChange,
                label = { Text(stringResource(Res.string.komf_providers_series_age_rating)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesAuthors,
                onCheckedChange = state::onSeriesAuthorsChange,
                label = { Text(stringResource(Res.string.komf_providers_series_authors)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesBookCount,
                onCheckedChange = state::onSeriesBookCountChange,
                label = { Text(stringResource(Res.string.komf_providers_series_book_count)) }
            )
            HorizontalDivider()
            SwitchWithLabel(
                checked = state.seriesCover,
                onCheckedChange = state::onSeriesCoverChange,
                label = { Text(stringResource(Res.string.komf_providers_series_cover)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesGenres,
                onCheckedChange = state::onSeriesGenresChange,
                label = { Text(stringResource(Res.string.komf_providers_series_genres)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesLinks,
                onCheckedChange = state::onSeriesLinksChange,
                label = { Text(stringResource(Res.string.komf_providers_series_links)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesPublisher,
                onCheckedChange = state::onSeriesPublisherChange,
                label = { Text(stringResource(Res.string.komf_providers_series_publisher)) }
            )
            HorizontalDivider()

            if (state.canHaveMultiplePublishers) {
                SwitchWithLabel(
                    checked = state.seriesOriginalPublisher,
                    onCheckedChange = state::onSeriesOriginalPublisherChange,
                    label = { Text(stringResource(Res.string.komf_providers_series_publisher_original)) },
                    supportingText = { Text(stringResource(Res.string.komf_providers_series_publisher_original_desc)) }
                )
                HorizontalDivider()
            }

            SwitchWithLabel(
                checked = state.seriesReleaseDate,
                onCheckedChange = state::onSeriesReleaseDateChange,
                label = { Text(stringResource(Res.string.komf_providers_series_release_date)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesStatus,
                onCheckedChange = state::onSeriesStatusChange,
                label = { Text(stringResource(Res.string.komf_providers_series_status)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesSummary,
                onCheckedChange = state::onSeriesSummaryChange,
                label = { Text(stringResource(Res.string.komf_providers_series_summary)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesTags,
                onCheckedChange = state::onSeriesTagsChange,
                label = { Text(stringResource(Res.string.komf_providers_series_tags)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesTitle,
                onCheckedChange = state::onSeriesTitleChange,
                label = { Text(stringResource(Res.string.komf_providers_series_title)) }
            )
        }
    }
}

private class BookMetadataTab(private val state: ProviderConfigState) : DialogTab {
    override fun options() = TabItem(title = Res.string.komf_edit_tab_book_metadata)

    @Composable
    override fun Content() {
        Column {
            SwitchWithLabel(
                checked = state.bookEnabled,
                onCheckedChange = state::onBookEnabledChange,
                label = { Text(stringResource(Res.string.komf_providers_book_enabled)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookAuthors,
                onCheckedChange = state::onBookAuthorsChange,
                label = { Text(stringResource(Res.string.komf_providers_book_authors)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookCover,
                onCheckedChange = state::onBookCoverChange,
                label = { Text(stringResource(Res.string.komf_providers_book_cover)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookIsbn,
                onCheckedChange = state::onBookIsbnChange,
                label = { Text(stringResource(Res.string.komf_providers_book_isbn)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookLinks,
                onCheckedChange = state::onBookLinksChange,
                label = { Text(stringResource(Res.string.komf_providers_book_links)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookNumber,
                onCheckedChange = state::onBookNumberChange,
                label = { Text(stringResource(Res.string.komf_providers_book_number)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookReleaseDate,
                onCheckedChange = state::onBookReleaseDateChange,
                label = { Text(stringResource(Res.string.komf_providers_book_release_date)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookSummary,
                onCheckedChange = state::onBookSummaryChange,
                label = { Text(stringResource(Res.string.komf_providers_book_summary)) }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookTags,
                onCheckedChange = state::onBookTagsChange,
                label = { Text(stringResource(Res.string.komf_providers_book_tags)) }
            )
        }
    }
}

private class ProviderSettingsTab(private val state: ProviderConfigState) : DialogTab {
    override fun options() = TabItem(title = Res.string.komf_edit_tab_provider_settings)

    @Composable
    override fun Content() {

        val unsetString = stringResource(Res.string.komf_providers_unset)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DropdownChoiceMenu(
                selectedOption = remember(state.mediaType) {
                    LabeledEntry(
                        state.mediaType,
                        state.mediaType?.name ?: unsetString
                    )
                },
                options = remember {
                    listOf(LabeledEntry<KomfMediaType?>(null, unsetString)) +
                            KomfMediaType.entries.map { LabeledEntry(it, it.name) }
                },
                onOptionChange = { state.onMediaTypeChange(it.value) },
                label = { Text(stringResource(Res.string.komf_providers_media_type)) },
                inputFieldModifier = Modifier.fillMaxWidth()
            )

            DropdownChoiceMenu(
                selectedOption = remember(state.nameMatchingMode) {
                    LabeledEntry(
                        state.nameMatchingMode,
                        state.nameMatchingMode?.name ?: unsetString
                    )
                },
                options = remember {
                    listOf(LabeledEntry<KomfNameMatchingMode?>(null, unsetString)) +
                            KomfNameMatchingMode.entries.map { LabeledEntry(it, it.name) }
                },
                onOptionChange = { state.onNameMatchingModeChange(it.value) },
                label = { Text(stringResource(Res.string.komf_providers_name_matching_mode)) },
                inputFieldModifier = Modifier.fillMaxWidth()
            )

            DropdownMultiChoiceMenu(
                selectedOptions = remember(state.authorRoles) { state.authorRoles.map { LabeledEntry(it, it.name) } },
                options = remember { KomfAuthorRole.entries.map { LabeledEntry(it, it.name) } },
                onOptionSelect = { state.onAuthorSelect(it.value) },
                label = { Text(stringResource(Res.string.komf_providers_author_roles)) },
                placeholder = unsetString,
                inputFieldModifier = Modifier.fillMaxWidth()
            )
            DropdownMultiChoiceMenu(
                selectedOptions = remember(state.artistRoles) { state.artistRoles.map { LabeledEntry(it, it.name) } },
                options = remember { KomfAuthorRole.entries.map { LabeledEntry(it, it.name) } },
                onOptionSelect = { state.onArtistSelect(it.value) },
                label = { Text(stringResource(Res.string.komf_providers_artist_roles)) },
                placeholder = unsetString,
                inputFieldModifier = Modifier.fillMaxWidth()
            )
            when (state) {
                is GenericProviderConfigState -> {}
                is AniListConfigState -> AniListProviderSettings(state)
                is MangaDexConfigState -> MangaDexProviderSettings(state)
                is MangaBakaConfigState -> MangaBakaProviderSettings(state)
            }

        }
    }

    @Composable
    private fun AniListProviderSettings(state: AniListConfigState) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SavableTextField(
                currentValue = remember(state.tagScoreThreshold) { state.tagScoreThreshold.toString() },
                onValueSave = { state.onTagScoreThresholdChange(it.toInt()) },
                valueChangePolicy = { it.toIntOrNull() != null },
                label = { Text(stringResource(Res.string.komf_providers_anilist_tag_score_threshold)) }
            )

            SavableTextField(
                currentValue = remember(state.tagSizeLimit) { state.tagSizeLimit.toString() },
                onValueSave = { state.onTagSizeLimitChange(it.toInt()) },
                valueChangePolicy = { it.toIntOrNull() != null },
                label = { Text(stringResource(Res.string.komf_providers_anilist_tag_score_threshold)) }
            )
        }
    }

    @Composable
    private fun MangaDexProviderSettings(state: MangaDexConfigState) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ChipFieldWithSuggestions(
                label = { Text(stringResource(Res.string.komf_providers_mangadex_alt_title_languages)) },
                values = state.coverLanguages,
                onValuesChange = state::onCoverLanguagesChange,
                suggestions = komfLanguageTagsSuggestions
            )
            DropdownMultiChoiceMenu(
                selectedOptions = state.links.map { LabeledEntry(it, it.name) },
                options = MangaDexLink.entries.map { LabeledEntry(it, it.name) },
                onOptionSelect = { state.onLinkSelect(it.value) },
                label = { Text(stringResource(Res.string.komf_providers_mangadex_inlcude_links)) },
                placeholder = stringResource(Res.string.komf_providers_mangadex_inlcude_links_all),
                inputFieldModifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun MangaBakaProviderSettings(state: MangaBakaConfigState) {
        HorizontalDivider()

        DropdownChoiceMenu(
            selectedOption = remember(state.mode) {
                LabeledEntry(
                    state.mode,
                    state.mode.name
                )
            },
            options = remember {
                MangaBakaMode.entries.map { LabeledEntry(it, it.name) }
            },
            onOptionChange = { state.onModeChange(it.value) },
            label = { Text(stringResource(Res.string.komf_providers_mangabaka_datasource_type)) },
            inputFieldModifier = Modifier.fillMaxWidth()
        )
    }
}
