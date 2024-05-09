package io.github.snd_r.komelia.ui.dialogs.readlistadd

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.readlist.KomgaReadList
import kotlinx.coroutines.launch

@Composable
fun AddToReadListDialog(
    books: List<KomgaBook>,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val viewmodel = remember { viewModelFactory.getAddToReadListDialogViewModel(books, onDismissRequest) }
    LaunchedEffect(books) { viewmodel.initialize() }
    AppDialog(
        header = { Header(onDismissRequest) },
        content = {
            DialogContent(
                books = books,
                readLists = viewmodel.readLists,
                onCreateNewReadList = viewmodel::createNew,
                onAddToReadList = viewmodel::addTo,
            )
        },
        onDismissRequest = onDismissRequest,
        modifier = Modifier.widthIn(max = 600.dp)
    )
}

@Composable
private fun Header(onDismissRequest: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Add to read list", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDismissRequest) { Icon(Icons.Default.Close, null) }
        }
        HorizontalDivider()
    }
}

@Composable
private fun DialogContent(
    books: List<KomgaBook>,
    readLists: List<KomgaReadList>,
    onCreateNewReadList: suspend (name: String) -> Unit,
    onAddToReadList: suspend (KomgaReadList) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Column(Modifier.padding(20.dp)) {
        var query by remember { mutableStateOf("") }
        val readListExistsForQuery = derivedStateOf { readLists.any { it.name == query } }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search or create read list") },
                supportingText = {
                    if (readListExistsForQuery.value)
                        Text(
                            "A read list with this name already exists",
                            color = MaterialTheme.colorScheme.error
                        )
                },
                modifier = Modifier.weight(1f)
            )
            FilledTonalButton(
                onClick = { coroutineScope.launch { onCreateNewReadList(query) } },
                shape = RoundedCornerShape(5.dp),
                enabled = query.isNotBlank() && !readListExistsForQuery.value,
                content = { Text("Create") },
            )
        }


        Surface(tonalElevation = 1.dp) {
            Column {
                val filteredReadLists = derivedStateOf { readLists.filter { it.name.contains(query) } }
                filteredReadLists.value.forEach { readList ->
                    ReadListEntry(
                        readList = readList,
                        alreadyContainsSeries = books.size == 1 && readList.bookIds.any { it == books.first().id },
                        onClick = { coroutineScope.launch { onAddToReadList(readList) } }
                    )
                }
            }
        }

    }

}

@Composable
private fun ReadListEntry(
    readList: KomgaReadList,
    alreadyContainsSeries: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .clickable(enabled = !alreadyContainsSeries) { onClick() }
            .fillMaxWidth()
            .padding(10.dp)
            .cursorForHand()
    ) {
        Text(readList.name)

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${readList.bookIds.size} books", style = MaterialTheme.typography.labelLarge)
            if (alreadyContainsSeries) Text(
                "already contains this book",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        HorizontalDivider()
    }
}