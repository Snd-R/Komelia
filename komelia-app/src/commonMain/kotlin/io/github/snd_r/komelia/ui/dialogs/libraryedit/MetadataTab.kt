package io.github.snd_r.komelia.ui.dialogs.libraryedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.ChildSwitchingCheckboxWithLabel
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem

internal class MetadataTab(
    private val vm: LibraryEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "METADATA",
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
            label = { Text("Import metadata for CBR/CBZ containing a ComicInfo.xml file") },
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
                label = { Text("Book metadata") },
                checked = importComicInfoBook.value,
                onCheckedChange = importComicInfoBook.setValue,
            )

            CheckboxWithLabel(
                label = { Text("Series metadata") },
                checked = importComicInfoSeries.value,
                onCheckedChange = importComicInfoSeries.setValue,
            )

            CheckboxWithLabel(
                label = { Text("Append volume to series title") },
                checked = importComicInfoSeriesAppendVolume.value,
                onCheckedChange = importComicInfoSeriesAppendVolume.setValue,
            )

            CheckboxWithLabel(
                label = { Text("Collections") },
                checked = importComicInfoCollection.value,
                onCheckedChange = importComicInfoCollection.setValue,
            )

            CheckboxWithLabel(
                label = { Text("Read lists") },
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
            label = { Text("Import metadata from EPUB files") },
            children = listOf(
                importEpubBook,
                importEpubSeries,
            ),
        )
        Column(Modifier.padding(start = 10.dp)) {
            CheckboxWithLabel(
                label = { Text("Book metadata") },
                checked = importEpubBook.value,
                onCheckedChange = importEpubBook.setValue,
            )
            CheckboxWithLabel(
                label = { Text("Series metadata") },
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
        Text("Import metadata generated by Mylar")
        Column(Modifier.padding(start = 10.dp)) {
            CheckboxWithLabel(
                label = { Text("Series metadata") },
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
        Text("Import local media assets")
        Column(Modifier.padding(start = 10.dp)) {
            CheckboxWithLabel(
                label = { Text("Local artwork") },
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
        Text("Import ISBN within barcode")
        Column(Modifier.padding(start = 10.dp)) {
            CheckboxWithLabel(
                label = { Text("ISBN barcode") },
                checked = importBarcodeIsbn.value,
                onCheckedChange = importBarcodeIsbn.setValue,
            )
        }
    }
}