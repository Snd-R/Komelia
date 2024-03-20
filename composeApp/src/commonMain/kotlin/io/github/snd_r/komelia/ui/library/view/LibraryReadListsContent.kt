package io.github.snd_r.komelia.ui.library.view

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
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.PaginationWithSizeOptions
import io.github.snd_r.komelia.ui.common.itemlist.PlaceHolderLazyCardGrid
import io.github.snd_r.komelia.ui.common.itemlist.ReadListLazyCardGrid
import io.github.snd_r.komga.readlist.KomgaReadList
import io.github.snd_r.komga.readlist.KomgaReadListId

@Composable
fun LibraryReadListsContent(
    readLists: List<KomgaReadList>,
    readListsTotalCount: Int,
    onReadListClick: (KomgaReadListId) -> Unit,
    onReadListDelete: (KomgaReadListId) -> Unit,
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
                    if (readListsTotalCount > 1) Text("$readListsTotalCount read lists")
                    else Text("$readListsTotalCount read list")
                },
                modifier = Modifier.padding(end = 10.dp)
            )

            PaginationWithSizeOptions(
                totalPages = totalPages,
                currentPage = currentPage,
                onPageChange = onPageChange,
                navigationButtons = false,
                pageSize = pageSize,
                onPageSizeChange = onPageSizeChange,
                spacer = { Spacer(Modifier.weight(1f)) }
            )
        }

        if (isLoading) {
            if (readListsTotalCount > pageSize) PlaceHolderLazyCardGrid(pageSize, minSize)
            else LoadingMaxSizeIndicator()
        } else {
            ReadListLazyCardGrid(
                readLists = readLists,
                onReadListClick = onReadListClick,
                onReadListDelete = onReadListDelete,
                minSize = minSize
            )
        }
    }
}