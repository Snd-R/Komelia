package io.github.snd_r.komelia.ui.dialogs.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.WindowSizeClass.COMPACT
import io.github.snd_r.komelia.platform.WindowSizeClass.EXPANDED
import io.github.snd_r.komelia.platform.WindowSizeClass.MEDIUM
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.dialogs.AppDialogLayout
import io.github.snd_r.komelia.ui.dialogs.BasicAppDialog
import io.github.snd_r.komelia.ui.dialogs.DialogConfirmCancelButtons

@Composable
fun TabDialog(
    modifier: Modifier = Modifier,
    title: String,
    currentTab: DialogTab,
    tabs: List<DialogTab>,
    onTabChange: (DialogTab) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    confirmationText: String,
    confirmEnabled: Boolean = true,
    showCancelButton: Boolean = true
) {

    val sizeModifier = when (LocalWindowWidth.current) {
        COMPACT -> Modifier.fillMaxSize()
        MEDIUM, EXPANDED -> Modifier.width(840.dp)
        else -> Modifier.width(1000.dp)
    }
    BasicAppDialog(modifier.then(sizeModifier), onDismissRequest) {
        when (LocalWindowWidth.current) {
            COMPACT, MEDIUM -> CompactTabDialog(
                title = title,
                currentTab = currentTab,
                tabs = tabs,
                showCancelButton = showCancelButton,
                canConfirm = confirmEnabled,
                onConfirm = onConfirm,
                confirmationText = confirmationText,
                onTabChange = onTabChange,
                onDismissRequest = onDismissRequest
            )

            else -> TabColumnDialog(
                title = title,
                currentTab = currentTab,
                tabs = tabs,

                showCancelButton = showCancelButton,
                canConfirm = confirmEnabled,
                onConfirm = onConfirm,
                confirmationText = confirmationText,
                onTabChange = onTabChange,
                onDismissRequest = onDismissRequest
            )
        }

    }
}

@Composable
private fun CompactTabDialog(
    title: String,
    currentTab: DialogTab,
    tabs: List<DialogTab>,

    showCancelButton: Boolean,
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
            if (showCancelButton) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.cursorForHand()
                ) {
                    Icon(Icons.Default.Close, null)
                }
            }
            Text(
                text = title,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 350.dp).weight(1f)
            )

            ElevatedButton(
                enabled = canConfirm,
                onClick = { onConfirm() },
                colors = ButtonDefaults.elevatedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
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

        Column(
            Modifier
                .fillMaxSize()
                .padding(5.dp)
                .verticalScroll(rememberScrollState())
        ) {
            currentTab.Content()
            Spacer(Modifier.imePadding())
        }
    }

}

@Composable
private fun TabColumnDialog(
    title: String,
    currentTab: DialogTab,
    tabs: List<DialogTab>,

    showCancelButton: Boolean,
    canConfirm: Boolean,
    onConfirm: () -> Unit,
    confirmationText: String,
    onTabChange: (DialogTab) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val scrollState = rememberScrollState()
    AppDialogLayout(
        header = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(20.dp)

            )
        },
        body = {
            Row(Modifier.padding(start = 2.dp)) {
                ColumnNavigationItems(
                    currentTab = currentTab,
                    tabs = tabs,
                    onTabChange = onTabChange
                )

                Column(
                    Modifier
                        .verticalScroll(scrollState)
                        .heightIn(min = 500.dp)
                        .padding(bottom = 10.dp, start = 10.dp, end = 30.dp)
                ) {
                    currentTab.Content()
                    Spacer(Modifier.imePadding())
                }
            }
        },
        scrollbar = { VerticalScrollbar(scrollState) },
        controlButtons = {
            DialogConfirmCancelButtons(
                confirmText = confirmationText,
                cancelText = "Cancel",
                onConfirm = onConfirm,
                confirmEnabled = canConfirm,
                showCancelButton = showCancelButton,
                onCancel = onDismissRequest,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        },
        contentPadding = PaddingValues(0.dp)
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
            icon = { tab.options().icon?.let { Icon(it, contentDescription = null, tint = color) } },
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