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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.snd_r.komelia.ui.common.AppTheme


@Composable
fun TabDialog(
    title: String,
    dialogSize: DpSize = DpSize(750.dp, Dp.Unspecified),
    currentTab: DialogTab,
    tabs: List<DialogTab>,
    controlButtons: @Composable () -> Unit = {},
    onTabChange: (DialogTab) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        val focusManager = LocalFocusManager.current
        val scrollState = rememberScrollState()
        Card(
            modifier = Modifier
                .widthIn(max = dialogSize.width)
                .heightIn(max = dialogSize.height)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.material.surface),
            border = BorderStroke(1.dp, AppTheme.colors.material.surfaceVariant)
        ) {
            Column {
                Text(
                    title,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(20.dp)
                )


                Row {
                    NavigationItems(
                        currentTab = currentTab,
                        tabs = tabs,
                        onTabChange = onTabChange
                    )

                    Box(Modifier.padding(10.dp)) {
                        currentTab.Content()
                    }

                }
                controlButtons()
            }

        }
    }
}

@Composable
fun DialogControlButtons(
    confirmationText: String,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Spacer(Modifier.weight(1f))

        TextButton(onClick = onDismissRequest) {
            Text("Cancel")
        }

        FilledTonalButton(onClick = { onConfirmClick() }) {
            Text(confirmationText)
        }
    }
}

@Composable
fun NavigationItems(
    currentTab: DialogTab,
    tabs: List<DialogTab>,
    onTabChange: (DialogTab) -> Unit,
) {
    val currentIndex = tabs.indexOf(currentTab)
    Column {
        TabColumn(selectedTabIndex = currentIndex) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == currentIndex
                val enabled = tab.options().enabled
                val color = when {
                    !enabled -> AppTheme.colors.material.surfaceVariant
                    selected -> AppTheme.colors.material.secondary
                    else -> AppTheme.colors.material.primary
                }

                TabNavigationItem(
                    label = {
                        Text(
                            tab.options().title,
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
        modifier = Modifier.selectable(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            role = Role.Tab,
        ).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.size(5.dp))
        label()
    }
}