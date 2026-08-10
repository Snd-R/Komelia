package snd.komelia.ui.dialogs.libraryedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_barcode
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_barcode_isbn
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_comicinfo
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_comicinfo_append_volume_to_series
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_comicinfo_book_metadata
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_comicinfo_collections
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_comicinfo_readlists
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_comicinfo_series_metadata
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_epub
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_epub_book_metadata
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_epub_series_metadata
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_local_artwork
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_local_media
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_mylar
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_import_mylar_series_metadata
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_tab_metadata
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.common.components.ChildSwitchingCheckboxWithLabel
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem

internal class MetadataTab(
    private val vm: LibraryEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = Res.string.library_edit_tab_metadata,
        icon = Icons.Default.Book
    )

    @Composable
    override fun Content() {
        MetadataTabContent(
            importComicInfoBook = StateHolder(vm.importComicInfoBook, vm::importComicInfoBook::set),
            importComicInfoSeries = StateHolder(vm.importComicInfoSeries, vm::importComicInfoSeries::set),
            importComicInfoSeriesAppendVolume = StateHolder(
                vm.importComicInfoSeriesAppendVolume,
                vm::importComicInfoSeriesAppendVolume::set
            ),
            importComicInfoCollection = StateHolder(
                vm.importComicInfoCollection,
                vm::importComicInfoCollection::set
            ),
            importComicInfoReadList = StateHolder(vm.importComicInfoReadList, vm::importComicInfoReadList::set),
            importEpubBook = StateHolder(vm.importEpubBook, vm::importEpubBook::set),
            importEpubSeries = StateHolder(vm.importEpubSeries, vm::importEpubSeries::set),
            importMylarSeries = StateHolder(vm.importMylarSeries, vm::importMylarSeries::set),
            importLocalArtwork = StateHolder(vm.importLocalArtwork, vm::importLocalArtwork::set),
            importBarcodeIsbn = StateHolder(vm.importBarcodeIsbn, vm::importBarcodeIsbn::set),
        )
    }
}


@Composable
private fun MetadataTabContent(
    importComicInfoBook: StateHolder<Boolean>,
    importComicInfoSeries: StateHolder<Boolean>,
    importComicInfoSeriesAppendVolume: StateHolder<Boolean>,
    importComicInfoCollection: StateHolder<Boolean>,
    importComicInfoReadList: StateHolder<Boolean>,
    importEpubBook: StateHolder<Boolean>,
    importEpubSeries: StateHolder<Boolean>,
    importMylarSeries: StateHolder<Boolean>,
    importLocalArtwork: StateHolder<Boolean>,
    importBarcodeIsbn: StateHolder<Boolean>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        ComicInfoSettings(
            importComicInfoBook = importComicInfoBook,
            importComicInfoSeries = importComicInfoSeries,
            importComicInfoSeriesAppendVolume = importComicInfoSeriesAppendVolume,
            importComicInfoCollection = importComicInfoCollection,
            importComicInfoReadList = importComicInfoReadList,
        )
        EpubSettings(
            importEpubBook = importEpubBook,
            importEpubSeries = importEpubSeries
        )
        MylarSettings(importMylarSeries)
        LocalArtworkSettings(importLocalArtwork)
        BarcodeISBNSettings(importBarcodeIsbn)


    }
}

@Composable
private fun ComicInfoSettings(
    importComicInfoBook: StateHolder<Boolean>,
    importComicInfoSeries: StateHolder<Boolean>,
    importComicInfoSeriesAppendVolume: StateHolder<Boolean>,
    importComicInfoCollection: StateHolder<Boolean>,
    importComicInfoReadList: StateHolder<Boolean>,
) {
    Column {
        ChildSwitchingCheckboxWithLabel(
            label = { Text(stringResource(Res.string.library_edit_import_comicinfo)) },
            children = listOf(
                importComicInfoBook,
                importComicInfoSeries,
                importComicInfoSeriesAppendVolume,
                importComicInfoCollection,
                importComicInfoReadList
            ),
        )
        Column(
            modifier = Modifier.padding(start = 10.dp)
        ) {
            CheckboxWithLabel(
                label = { Text(stringResource(Res.string.library_edit_import_comicinfo_book_metadata)) },
                checked = importComicInfoBook.value,
                onCheckedChange = importComicInfoBook.setValue,
            )

            CheckboxWithLabel(
                label = { Text(stringResource(Res.string.library_edit_import_comicinfo_series_metadata)) },
                checked = importComicInfoSeries.value,
                onCheckedChange = importComicInfoSeries.setValue,
            )

            CheckboxWithLabel(
                label = { Text(stringResource(Res.string.library_edit_import_comicinfo_append_volume_to_series)) },
                checked = importComicInfoSeriesAppendVolume.value,
                onCheckedChange = importComicInfoSeriesAppendVolume.setValue,
            )

            CheckboxWithLabel(
                label = { Text(stringResource(Res.string.library_edit_import_comicinfo_collections)) },
                checked = importComicInfoCollection.value,
                onCheckedChange = importComicInfoCollection.setValue,
            )

            CheckboxWithLabel(
                label = { Text(stringResource(Res.string.library_edit_import_comicinfo_readlists)) },
                checked = importComicInfoReadList.value,
                onCheckedChange = importComicInfoReadList.setValue,
            )
        }
    }
}

@Composable
private fun EpubSettings(
    importEpubBook: StateHolder<Boolean>,
    importEpubSeries: StateHolder<Boolean>,
) {
    Column {
        ChildSwitchingCheckboxWithLabel(
            label = { Text(stringResource(Res.string.library_edit_import_epub)) },
            children = listOf(
                importEpubBook,
                importEpubSeries,
            ),
        )
        Column(Modifier.padding(start = 10.dp)) {
            CheckboxWithLabel(
                label = { Text(stringResource(Res.string.library_edit_import_epub_book_metadata)) },
                checked = importEpubBook.value,
                onCheckedChange = importEpubBook.setValue,
            )
            CheckboxWithLabel(
                label = { Text(stringResource(Res.string.library_edit_import_epub_series_metadata)) },
                checked = importEpubSeries.value,
                onCheckedChange = importEpubSeries.setValue,
            )
        }
    }
}

@Composable
private fun MylarSettings(
    importMylarSeries: StateHolder<Boolean>,
) {
    Column {
        Text(stringResource(Res.string.library_edit_import_mylar))
        Column(Modifier.padding(start = 10.dp)) {
            CheckboxWithLabel(
                label = { Text(stringResource(Res.string.library_edit_import_mylar_series_metadata)) },
                checked = importMylarSeries.value,
                onCheckedChange = importMylarSeries.setValue,
            )
        }
    }
}

@Composable
private fun LocalArtworkSettings(
    importLocalArtwork: StateHolder<Boolean>,
) {

    Column {
        Text(stringResource(Res.string.library_edit_import_local_media))
        Column(Modifier.padding(start = 10.dp)) {
            CheckboxWithLabel(
                label = { Text(stringResource(Res.string.library_edit_import_local_artwork)) },
                checked = importLocalArtwork.value,
                onCheckedChange = importLocalArtwork.setValue,
            )
        }
    }
}

@Composable
private fun BarcodeISBNSettings(
    importBarcodeIsbn: StateHolder<Boolean>,
) {

    Column {
        Text(stringResource(Res.string.library_edit_import_barcode))
        Column(Modifier.padding(start = 10.dp)) {
            CheckboxWithLabel(
                label = { Text(stringResource(Res.string.library_edit_import_barcode_isbn)) },
                checked = importBarcodeIsbn.value,
                onCheckedChange = importBarcodeIsbn.setValue,
            )
        }
    }
}