package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScrollableItemsRow(
    label: String,
    content: List<@Composable () -> Unit>,
) {
    if (content.isEmpty()) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val scrollState = rememberLazyListState()
        Text(
            label,
            fontSize = 12.sp,
            modifier = Modifier.width(150.dp)
        )
        LazyRow(state = scrollState, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            items(content) {
                it()
            }

        }
    }
}

@Composable
fun ScrollableItemsRow(
    label: String,
    content: @Composable () -> Unit,
) {
    ScrollableItemsRow(label, listOf { content() })
}

@Composable
fun DescriptionChips(
    label: String,
    chipValue: String,
) {
    if (chipValue.isBlank()) return
    DescriptionChips(label, listOf(chipValue))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DescriptionChips(
    label: String,
    chipValues: List<String>,
    secondaryValues: List<String>? = null,
    onChipClick: (String) -> Unit = {}
) {
    if (chipValues.isEmpty() && secondaryValues.isNullOrEmpty()) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            label,
            fontSize = 12.sp,
            modifier = Modifier.width(150.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            chipValues.forEach {
                MetadataChip(onClick = { onChipClick(it) }) {
                    Text(it, fontSize = 12.sp, lineHeight = 0.sp)
                }
            }
            secondaryValues?.filter { it !in chipValues }?.forEach {
                MetadataChip(borderColor = MaterialTheme.colorScheme.primary, onClick = { onChipClick(it) }) {
                    Text(it, fontSize = 12.sp, lineHeight = 0.sp)
                }
            }

        }

    }
}


@Composable
fun MetadataChip(
    borderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp, 5.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }

}
