package snd.komelia.ui.library.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_tab_collections_count
import org.jetbrains.compose.resources.pluralStringResource
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.common.components.PageSizeSelectionDropdown
import snd.komelia.ui.common.itemlist.CollectionLazyCardGrid
import snd.komelia.ui.common.itemlist.PlaceHolderLazyCardGrid
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionId

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
                    pluralStringResource(
                        Res.plurals.library_tab_collections_count,
                        collectionsTotalCount,
                        collectionsTotalCount
                    )
                },
                modifier = Modifier.padding(end = 10.dp)
            )

            Spacer(Modifier.weight(1f))
            PageSizeSelectionDropdown(pageSize, onPageSizeChange)
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