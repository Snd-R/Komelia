package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand

@Composable
fun SwitchWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: @Composable () -> Unit,
    supportingText: @Composable () -> Unit = {},
    supportingTextColor: Color = LocalContentColor.current.copy(alpha = 0.6f),
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val contentColor = if (enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = .4f)
    val actualSupportingTextColor = if (enabled) supportingTextColor else LocalContentColor.current.copy(alpha = .4f)
    val colors = SwitchDefaults.colors(
        checkedTrackColor = MaterialTheme.colorScheme.secondary,
        checkedThumbColor = MaterialTheme.colorScheme.onSecondary
    )
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = modifier.clickable(
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            ).cursorForHand().padding(contentPadding),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                label()
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = enabled,
                    colors = colors,
                    modifier = Modifier.scale(1f, .9f)
                )
            }

            CompositionLocalProvider(LocalContentColor provides actualSupportingTextColor) {
                supportingText()
            }
        }
    }
}