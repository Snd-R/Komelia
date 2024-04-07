package io.github.snd_r.komelia.ui.library.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.intEntry
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.itemlist.CollectionLazyCardGrid
import io.github.snd_r.komelia.ui.common.itemlist.PlaceHolderLazyCardGrid
import io.github.snd_r.komga.collection.KomgaCollection
import io.github.snd_r.komga.collection.KomgaCollectionId

@Composable
fun LibraryCollectionsContent(
    collections: List<KomgaCollection>,
    collectionsTotalCount: Int,
    onCollectionClick: (KomgaCollectionId) -> Unit,
    onCollectionDelete: (KomgaCollectionId) -> Unit,
    isLoading: Boolean,

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    minSize: Dp
) {
    Column(verticalArrangement = Arrangement.Center) {

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
        ) {
            SuggestionChip(
                onClick = {},
                label = {
                    if (collectionsTotalCount > 1) Text("$collectionsTotalCount collections")
                    else Text("$collectionsTotalCount collection")
                },
                modifier = Modifier.padding(end = 10.dp)
            )

            Spacer(Modifier.weight(1f))

            DropdownChoiceMenu(
                selectedOption = intEntry(pageSize),
                options = listOf(
                    intEntry(20),
                    intEntry(50),
                    intEntry(100),
                    intEntry(200),
                    intEntry(500)
                ),
                onOptionChange = { onPageSizeChange(it.value) },
                contentPadding = PaddingValues(5.dp),
                textFieldModifier = Modifier
                    .widthIn(min = 70.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .padding(end = 10.dp)
            )
        }

        if (isLoading) {
            if (collectionsTotalCount > pageSize) PlaceHolderLazyCardGrid(pageSize, minSize)
            else LoadingMaxSizeIndicator()
        } else {
            CollectionLazyCardGrid(
                collections = collections,
                onCollectionClick = onCollectionClick,
                onCollectionDelete = onCollectionDelete,
                totalPages = totalPages,
                currentPage = currentPage,
                onPageChange = onPageChange,
                minSize = minSize
            )
        }
    }
}