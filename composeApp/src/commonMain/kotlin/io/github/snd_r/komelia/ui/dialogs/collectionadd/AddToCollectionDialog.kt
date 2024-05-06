package io.github.snd_r.komelia.ui.dialogs.collectionadd

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
import io.github.snd_r.komga.collection.KomgaCollection
import io.github.snd_r.komga.series.KomgaSeries
import kotlinx.coroutines.launch

@Composable
fun AddToCollectionDialog(
    series: KomgaSeries,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val viewmodel = remember { viewModelFactory.getAddToCollectionDialogViewModel(series, onDismissRequest) }
    LaunchedEffect(series) { viewmodel.initialize() }
    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        onDismissRequest = onDismissRequest,
        header = { Header(onDismissRequest) },
        content = {
            DialogContent(
                series = series,
                collections = viewmodel.collections,
                onCreateNewCollection = viewmodel::createNew,
                onAddToCollection = viewmodel::addTo,
            )
        },
    )
}

@Composable
private fun Header(onDismissRequest: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Add to collection", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDismissRequest) { Icon(Icons.Default.Close, null) }
        }
        HorizontalDivider()
    }
}

@Composable
private fun DialogContent(
    series: KomgaSeries,
    collections: List<KomgaCollection>,
    onCreateNewCollection: suspend (name: String) -> Unit,
    onAddToCollection: suspend (KomgaCollection) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Column(Modifier.padding(20.dp)) {
        var query by remember { mutableStateOf("") }
        val collectionExistsForQuery = derivedStateOf { collections.any { it.name == query } }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search or create collection") },
                supportingText = {
                    if (collectionExistsForQuery.value)
                        Text(
                            "A collection with this name already exists",
                            color = MaterialTheme.colorScheme.error
                        )
                },
                modifier = Modifier.weight(1f)
            )
            FilledTonalButton(
                onClick = { coroutineScope.launch { onCreateNewCollection(query) } },
                shape = RoundedCornerShape(5.dp),
                enabled = query.isNotBlank() && !collectionExistsForQuery.value,
                content = { Text("Create") },
            )
        }


        Surface(tonalElevation = 1.dp) {
            Column {
                val filteredCollections = derivedStateOf { collections.filter { it.name.contains(query) } }
                filteredCollections.value.forEach { collection ->
                    CollectionEntry(
                        collection = collection,
                        alreadyContainsSeries = collection.seriesIds.any() { it == series.id },
                        onClick = { coroutineScope.launch { onAddToCollection(collection) } }
                    )
                }
            }
        }

    }

}

@Composable
private fun CollectionEntry(
    collection: KomgaCollection,
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
        Text(collection.name)

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${collection.seriesIds.size} series", style = MaterialTheme.typography.labelLarge)
            if (alreadyContainsSeries) Text(
                "already contains this series",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        HorizontalDivider()
    }
}