package snd.komelia.ui.settings.komf

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import snd.komelia.ui.platform.cursorForHand
import snd.komf.api.mediaserver.KomfMediaServerLibrary
import snd.komf.api.mediaserver.KomfMediaServerLibraryId

private fun <T> tabTransitionSpec(): AnimatedContentTransitionScope<IndexedState<T>>.() -> ContentTransform = {
    if (targetState.index > initialState.index) {
        slideInHorizontally { w -> w } togetherWith slideOutHorizontally { w -> -w }
    } else {
        slideInHorizontally { w -> -w } togetherWith slideOutHorizontally { w -> w }
    }
}

private data class IndexedState<T>(val index: Int, val state: T)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> LibraryTabs(
    defaultProcessingState: T,
    libraryProcessingState: Map<KomfMediaServerLibraryId, T>,

    onLibraryConfigAdd: (libraryId: KomfMediaServerLibraryId) -> Unit,
    onLibraryConfigRemove: (libraryId: KomfMediaServerLibraryId) -> Unit,
    libraries: List<KomfMediaServerLibrary>,
    content: @Composable (T) -> Unit,
) {

    Column {
        var selectedTabIndex by remember { mutableStateOf(0) }
        var selectedState by remember(defaultProcessingState) { mutableStateOf(defaultProcessingState) }
        Row {

            var isExpanded by remember { mutableStateOf(false) }


            FlowRow {
                TextButton(
                    modifier = Modifier.cursorForHand(),
                    colors = if (selectedTabIndex == 0) ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) else ButtonDefaults.textButtonColors(),
                    onClick = {
                        selectedTabIndex = 0
                        selectedState = defaultProcessingState
                    },
                ) {
                    Text(
                        "Default",
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }

                libraryProcessingState.entries.forEachIndexed { index, (libraryId, state) ->
                    TextButton(
                        modifier = Modifier.cursorForHand(),
                        colors =
                            if (selectedTabIndex == index + 1) ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ) else ButtonDefaults.textButtonColors(),
                        onClick = {
                            selectedTabIndex = index + 1
                            selectedState = state
                        },
                    ) {
                        Text(
                            libraries.firstOrNull { it.id == libraryId }?.name
                                ?: "Unknown library ${libraryId.value}",
                            overflow = TextOverflow.Ellipsis, maxLines = 1
                        )
                        Box(
                            Modifier
                                .clickable {
                                    onLibraryConfigRemove(libraryId)
                                    if (selectedTabIndex == index + 1) {
                                        selectedTabIndex = 0
                                        selectedState = defaultProcessingState
                                    }
                                }
                                .padding(3.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it },
            ) {
                IconButton(
                    onClick = { isExpanded = true },
                    modifier = Modifier
                        .cursorForHand()
                        .menuAnchor(PrimaryNotEditable),

                    ) {
                    Icon(Icons.Default.Add, null)
                }

                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    modifier = Modifier.widthIn(min = 200.dp)
                ) {

                    libraries.filter { !libraryProcessingState.containsKey(it.id) }.forEach { library ->
                        DropdownMenuItem(
                            modifier = Modifier.cursorForHand(),
                            text = { Text(library.name) },
                            onClick = {
                                isExpanded = false
                                onLibraryConfigAdd(library.id)
                            }
                        )
                    }
                }
            }
        }

        HorizontalDivider(Modifier.padding(bottom = 5.dp))
        AnimatedContent(
            targetState = IndexedState(selectedTabIndex, selectedState),
            transitionSpec = tabTransitionSpec(),
            contentKey = { it.index }
        ) {
            content(it.state)
        }

    }
}
