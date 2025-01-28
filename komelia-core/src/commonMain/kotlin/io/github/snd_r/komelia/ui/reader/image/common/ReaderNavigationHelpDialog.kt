package io.github.snd_r.komelia.ui.reader.image.common

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowSizeClass.COMPACT
import io.github.snd_r.komelia.platform.WindowSizeClass.EXPANDED
import io.github.snd_r.komelia.platform.WindowSizeClass.FULL
import io.github.snd_r.komelia.platform.WindowSizeClass.MEDIUM
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState.ReadingDirection.RIGHT_TO_LEFT
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM

@Composable
fun PagedReaderHelpDialog(
    onDismissRequest: () -> Unit,
) {
    AppDialog(
        modifier = Modifier.fillMaxWidth(.9f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        content = {
            when (LocalWindowWidth.current) {
                COMPACT, MEDIUM, EXPANDED -> Column { PagedDialogContent() }
                FULL -> Row { PagedDialogContent(Modifier.weight(1f)) }
            }
        }
    )
}

@Composable
fun ContinuousReaderHelpDialog(
    readingDirection: ContinuousReaderState.ReadingDirection,
    onDismissRequest: () -> Unit,
) {
    val orientation = remember(readingDirection) {
        when (readingDirection) {
            TOP_TO_BOTTOM -> Vertical
            LEFT_TO_RIGHT, RIGHT_TO_LEFT -> Horizontal
        }
    }
    AppDialog(
        modifier = Modifier.fillMaxWidth(.9f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        content = {
            when (LocalWindowWidth.current) {
                COMPACT, MEDIUM, EXPANDED -> Column { ContinuousDialogContent(orientation) }
                FULL -> Row { ContinuousDialogContent(orientation, Modifier.weight(1f)) }
            }
        }
    )
}

@Composable
private fun PagedDialogContent(
    elementsModifier: Modifier = Modifier,
) {
    val platform = LocalPlatform.current
    KeyDescriptionColumn(
        "Reader Navigation",
        mapOf(
            listOf("←") to "Previous page",
            listOf("→") to "Next page",
            listOf("Home") to "First page",
            listOf("End") to "Last page",
            if (platform == PlatformType.WEB_KOMF) {
                listOf("Shift", "Scroll Wheel") to "Zoom"
            } else {
                listOf("Ctrl", "Scroll Wheel") to "Zoom"
            }
        ),
        elementsModifier
    )

    KeyDescriptionColumn(
        "Settings",
        mapOf(
            listOf("L") to "Left to right",
            listOf("R") to "Right to left",
            listOf("C") to "Cycle scale",
            listOf("D") to "Cycle page layout",
            listOf("O") to "Toggle double page offset",
            listOf("F11") to "Enter/exit full screen"
        ),
        elementsModifier
    )

    KeyDescriptionColumn(
        "Menus",
        mapOf(
            listOf("M") to "Show/hide menu",
            listOf("H") to "Show/hide help",
            listOf("ALT", "←") to "Return to series screen"
        ),
        elementsModifier
    )
}

@Composable
private fun ContinuousDialogContent(
    orientation: Orientation,
    elementsModifier: Modifier = Modifier,
) {
    val platform = LocalPlatform.current
    val scrollDirection = when (orientation) {
        Vertical -> mapOf(
            listOf("↑") to "Scroll up",
            listOf("↓") to "Scroll down",
        )

        Horizontal -> mapOf(
            listOf("←") to "Scroll left",
            listOf("→") to "Scroll right",
        )
    }
    KeyDescriptionColumn(
        "Reader Navigation",
        scrollDirection + mapOf(
            listOf("Home") to "First page",
            listOf("End") to "Last page",
            if (platform == PlatformType.WEB_KOMF) {
                listOf("Shift", "Scroll Wheel") to "Zoom"
            } else {
                listOf("Ctrl", "Scroll Wheel") to "Zoom"
            }
        ),
        elementsModifier
    )

    KeyDescriptionColumn(
        "Settings",
        mapOf(
            listOf("V") to "Top to bottom",
            listOf("L") to "Left to right",
            listOf("R") to "Right to left",
            listOf("F11") to "Enter/exit full screen"
        ),
        elementsModifier
    )

    KeyDescriptionColumn(
        "Menus",
        mapOf(
            listOf("M") to "Show/hide menu",
            listOf("H") to "Show/hide help",
            listOf("ALT", "←") to "Return to series screen"
        ),
        elementsModifier
    )
}

@Composable
private fun KeyDescriptionColumn(
    title: String,
    keyToDescription: Map<List<String>, String>,
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.padding(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Row {
            Text(
                text = "Key",
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Description",
                modifier = Modifier.weight(1f)
            )
        }

        keyToDescription.forEach { (keys, description) ->
            HorizontalDivider()
            Row {

                ShortcutKeys(keys, Modifier.weight(1f))
                Text(description, Modifier.weight(1f))
            }
        }

    }
}

@Composable
private fun ShortcutKeys(keys: List<String>, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        ShortcutKey(keys.first())
        keys.drop(1).forEach { key ->
            Text(" + ")
            ShortcutKey(key)
        }
    }
}

@Composable
private fun ShortcutKey(label: String) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontWeight = FontWeight.Bold
        )
    }
}