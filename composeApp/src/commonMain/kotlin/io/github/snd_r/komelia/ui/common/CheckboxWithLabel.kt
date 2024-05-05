package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState.Indeterminate
import androidx.compose.ui.state.ToggleableState.Off
import androidx.compose.ui.state.ToggleableState.On
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand


@Composable
fun CheckboxWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: @Composable () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.secondary,
        checkmarkColor = MaterialTheme.colorScheme.onSecondary
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onCheckedChange(!checked) }
            .padding(10.dp)
            .cursorForHand()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = colors,
            enabled = enabled
        )
        Spacer(Modifier.size(10.dp))
        label()
    }
}

@Composable
fun ChildSwitchingCheckboxWithLabel(
    children: List<StateHolder<Boolean>>,
    label: @Composable () -> Unit
) {
    val state = remember(children) {
        val count = children.count { it.value }
        mutableStateOf(
            when (count) {
                children.size -> On
                0 -> Off
                else -> Indeterminate
            }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                when (state.value) {
                    On, Indeterminate -> {
                        state.value = Off
                        children.forEach { it.setValue(false) }
                    }

                    Off -> {
                        state.value = On
                        children.forEach { it.setValue(true) }
                    }
                }
            }
            .padding(10.dp)
            .cursorForHand()
    ) {
        TriStateCheckbox(
            state = state.value,
            onClick = null,
        )
        Spacer(Modifier.size(10.dp))
        label()
    }
}
