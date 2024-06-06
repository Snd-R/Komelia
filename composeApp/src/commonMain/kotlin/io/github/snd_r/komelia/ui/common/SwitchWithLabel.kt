package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.snd_r.komelia.platform.cursorForHand

@Composable
fun SwitchWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: @Composable () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = SwitchDefaults.colors(
        checkedTrackColor = MaterialTheme.colorScheme.secondary,
        checkedThumbColor = MaterialTheme.colorScheme.onSecondary
    )

    Row(
        modifier = modifier.clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onCheckedChange(!checked) }
        ).cursorForHand(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        label()
        Spacer(Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = colors,
        )
    }
}