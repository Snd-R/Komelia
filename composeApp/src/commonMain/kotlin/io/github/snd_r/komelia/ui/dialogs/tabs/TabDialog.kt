package io.github.snd_r.komelia.ui.dialogs.tabs

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.platform.WindowWidth.EXPANDED
import io.github.snd_r.komelia.platform.WindowWidth.MEDIUM
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalWindowWidth
import kotlin.math.roundToInt

@Composable
fun TabDialog(
    title: String,
    currentTab: DialogTab,
    tabs: List<DialogTab>,
    onTabChange: (DialogTab) -> Unit,
    onConfirm: () -> Unit,
    confirmationText: String,
    onDismissRequest: () -> Unit,
    canConfirm: Boolean = true,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        val focusManager = LocalFocusManager.current
        val sizeModifier = when (LocalWindowWidth.current) {
            COMPACT -> Modifier.fillMaxSize()
            MEDIUM, EXPANDED -> Modifier.width(840.dp)
            else -> Modifier.width(1000.dp)
        }
        Card(
            modifier = sizeModifier
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            when (LocalWindowWidth.current) {
                COMPACT, MEDIUM -> CompactTabDialog(
                    title = title,
                    currentTab = currentTab,
                    tabs = tabs,
                    canConfirm = canConfirm,
                    onConfirm = onConfirm,
                    confirmationText = confirmationText,
                    onTabChange = onTabChange,
                    onDismissRequest = onDismissRequest
                )

                else -> TabColumnDialog(
                    title = title,
                    currentTab = currentTab,
                    tabs = tabs,
                    canConfirm = canConfirm,
                    onConfirm = onConfirm,
                    confirmationText = confirmationText,
                    onTabChange = onTabChange,
                    onDismissRequest = onDismissRequest
                )
            }


        }
    }
}

@Composable
private fun CompactTabDialog(
    title: String,
    currentTab: DialogTab,
    tabs: List<DialogTab>,
    canConfirm: Boolean,
    onConfirm: () -> Unit,
    confirmationText: String,
    onTabChange: (DialogTab) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier.cursorForHand()
            ) {
                Icon(Icons.Default.Close, null)
            }
            Text(
                text = title,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 350.dp).weight(1f)
            )

            TextButton(
                enabled = canConfirm,
                onClick = { onConfirm() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text(confirmationText, fontWeight = FontWeight.Bold)
            }
        }

        val currentIndex = tabs.indexOf(currentTab)
        ScrollableTabRow(
            selectedTabIndex = currentIndex,
            indicator = { tabPositions ->
                if (currentIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentIndex]),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            divider = {}

        ) {
            TabNavigationItems(currentIndex, tabs, onTabChange)
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(5.dp)
                .verticalScroll(rememberScrollState())
        ) {
            currentTab.Content()
        }
    }

}

@Composable
private fun TabColumnDialogLayout(
    title: @Composable () -> Unit,
    body: @Composable () -> Unit,
    controlButtons: @Composable () -> Unit,
    scrollbar: @Composable () -> Unit,
) {
    SubcomposeLayout { constraints ->
        val titlePlaceable = subcompose("title", title).map { it.measure(Constraints()) }.first()
        val controlButtonsPlaceable = subcompose("controls", controlButtons).map { it.measure(Constraints()) }.first()

        val dialogMaxHeight = (constraints.maxHeight * 0.9).roundToInt()
        val resizedBodyPlaceable = subcompose("body", body)
            .map {
                val maxHeight = dialogMaxHeight - titlePlaceable.height - controlButtonsPlaceable.height
                it.measure(
                    Constraints(
                        minHeight = if (maxHeight < 500) maxHeight else 500,
                        maxHeight = maxHeight,
                        maxWidth = constraints.maxWidth
                    )
                )
            }.first()

        val scrollbarPlaceable = subcompose("scrollbar", scrollbar)
            .map { it.measure(constraints.copy(maxHeight = resizedBodyPlaceable.height)) }
            .first()

        layout(
            width = constraints.maxWidth,
            height = titlePlaceable.height + resizedBodyPlaceable.height + controlButtonsPlaceable.height
        ) {

            titlePlaceable.placeRelative(0, 0)

            resizedBodyPlaceable.placeRelative(0, titlePlaceable.height)

            controlButtonsPlaceable.placeRelative(
                constraints.maxWidth - controlButtonsPlaceable.width,
                titlePlaceable.height + resizedBodyPlaceable.height
            )
            scrollbarPlaceable.placeRelative(constraints.maxWidth - scrollbarPlaceable.width, titlePlaceable.height)
        }
    }

}

@Composable
private fun TabColumnDialog(
    title: String,
    currentTab: DialogTab,
    tabs: List<DialogTab>,
    canConfirm: Boolean,
    onConfirm: () -> Unit,
    confirmationText: String,
    onTabChange: (DialogTab) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val scrollState = rememberScrollState()
    TabColumnDialogLayout(
        title = {
            Text(
                text = title,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(20.dp)
            )
        },
        body = {
            Row {
                ColumnNavigationItems(
                    currentTab = currentTab,
                    tabs = tabs,
                    onTabChange = onTabChange
                )

                Box(
                    Modifier
                        .verticalScroll(scrollState)
                        .padding(bottom = 10.dp, start = 10.dp, end = 30.dp)
                ) {
                    currentTab.Content()
                }
            }
        },
        scrollbar = { VerticalScrollbar(scrollState) },
        controlButtons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand()
                ) {
                    Text("Cancel")
                }

                FilledTonalButton(
                    enabled = canConfirm,
                    onClick = { onConfirm() },
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand()
                ) {
                    Text(confirmationText)
                }
            }
        },
    )
}

@Composable
fun ColumnNavigationItems(
    currentTab: DialogTab,
    tabs: List<DialogTab>,
    onTabChange: (DialogTab) -> Unit,
) {
    val currentIndex = tabs.indexOf(currentTab)
    TabColumn(selectedTabIndex = currentIndex) {
        TabNavigationItems(currentIndex, tabs, onTabChange)
    }
}

@Composable
private fun TabNavigationItems(
    currentIndex: Int,
    tabs: List<DialogTab>,
    onTabChange: (DialogTab) -> Unit,
) {
    tabs.forEachIndexed { index, tab ->
        val selected = index == currentIndex
        val enabled = tab.options().enabled
        val color = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant
            selected -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.primary
        }

        TabNavigationItem(
            label = {
                Text(
                    text = tab.options().title,
                    color = color,
                    style = MaterialTheme.typography.labelLarge
                )
            },
            icon = { Icon(tab.options().icon, contentDescription = null, tint = color) },
            selected = selected,
            enabled = enabled,
            onClick = { onTabChange(tab) }
        )

    }
}

@Composable
private fun TabNavigationItem(
    label: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
            )
            .cursorForHand()
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.size(5.dp))
        label()
    }
}