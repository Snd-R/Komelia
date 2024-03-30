package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownChoiceMenu(
    selectedOption: T,
    options: List<T>,
    onOptionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    inputFieldColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(10.dp)
) {
    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        InputField(
            value = selectedOption.toString(),
            modifier = Modifier
                .menuAnchor()
                .then(modifier),
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            color = inputFieldColor,
            contentPadding = contentPadding
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {

            options.forEach {
                DropdownMenuItem(
                    text = { Text(it.toString()) },
                    onClick = {
                        onOptionChange(it)
                        isExpanded = false
                    }, modifier = Modifier.cursorForHand()
                )
            }
        }

    }
}

@Composable
private fun InputField(
    value: String,
    modifier: Modifier,
    label: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit),
    color: Color,
    contentPadding: PaddingValues = PaddingValues(10.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
//        tonalElevation = .3f.dp,
        shadowElevation = 1.dp,
        color = color,
        modifier = Modifier
            .cursorForHand()
            .indication(interactionSource, LocalIndication.current)
            .hoverable(interactionSource)
            .then(modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                horizontalAlignment = Alignment.Start
            ) {

                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelMedium) {
                    label?.let { it() }
                }
                Text(value)
            }

            trailingIcon()
        }
    }
}