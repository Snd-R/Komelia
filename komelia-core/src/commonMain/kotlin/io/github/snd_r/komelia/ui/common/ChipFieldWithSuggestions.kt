package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.m3.ChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowSizeClass.COMPACT
import io.github.snd_r.komelia.platform.WindowSizeClass.EXPANDED
import io.github.snd_r.komelia.platform.WindowSizeClass.FULL
import io.github.snd_r.komelia.platform.WindowSizeClass.MEDIUM
import io.github.snd_r.komelia.platform.scrollbar
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalWindowHeight
import kotlinx.coroutines.flow.drop

@Composable
fun ChipFieldWithSuggestions(
    label: @Composable () -> Unit,
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    suggestions: List<LabeledEntry<String>>,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val chipState = rememberChipTextFieldState(values.map { Chip(it) })
    LaunchedEffect(Unit) {
        snapshotFlow { chipState.chips.map { it.text } }
            .drop(1)
            .collect { onValuesChange(it) }
    }

    var textValue by remember { mutableStateOf(TextFieldValue()) }
    val suggestedOptions = derivedStateOf {
        suggestions.filter { (value, label) ->
            !values.contains(value) && label.lowercase().contains(textValue.text.lowercase())
        }
    }
    var isExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(textValue) {
        snapshotFlow { chipState.isTextFieldFocused }
            .collect { focused -> isExpanded = focused }
    }
    BoxWithConstraints {
        ChipTextField(
            state = chipState,
            value = textValue,
            onValueChange = { textValue = it },
            onSubmit = { value ->
                if (chipState.chips.none { it.text == value.text }) Chip(value.text)
                else {
                    textValue = TextFieldValue()
                    null
                }
            },
            readOnlyChips = true,
            label = label,
        )


        val windowHeightClass = LocalWindowHeight.current
        val platform = LocalPlatform.current
        // TODO better way to account for keyboard on mobile
        val dropdownHeight = remember(windowHeightClass) {
            when (windowHeightClass) {
                COMPACT, MEDIUM -> if (platform == PlatformType.MOBILE) 70.dp else 200.dp
                EXPANDED -> if (platform == PlatformType.MOBILE) 200.dp else 400.dp
                FULL -> 400.dp
            }
        }
        val scrollState = rememberScrollState()
        if (suggestedOptions.value.isNotEmpty()) {
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = {},
                properties = PopupProperties(focusable = false),
                scrollState = scrollState,
                modifier = Modifier
                    .scrollbar(scrollState, Orientation.Vertical)
                    .heightIn(max = dropdownHeight)
                    .width(maxWidth),
            ) {
                suggestedOptions.value.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            isExpanded = false
                            focusManager.clearFocus()
                            if (chipState.chips.none { it.text == key }) {
                                chipState.addChip(Chip(key))
                            }
                            textValue = TextFieldValue()
                        },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                    )
                }
            }

        }
    }
}
