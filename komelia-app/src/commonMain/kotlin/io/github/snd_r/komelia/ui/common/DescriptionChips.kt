package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun <T> DescriptionChips(
    label: String,
    chipValue: LabeledEntry<T>,
    onClick: (T) -> Unit,
    modifier: Modifier
) {
    DescriptionChips(
        label = label,
        chipValues = listOf(chipValue),
        onChipClick = onClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> DescriptionChips(
    label: String,
    chipValues: List<LabeledEntry<T>>,
    secondaryValues: List<LabeledEntry<T>>? = null,
    onChipClick: (T) -> Unit = {},
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    if (chipValues.isEmpty() && secondaryValues.isNullOrEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
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
            chipValues.forEach { entry ->
                NoPaddingChip(onClick = { onChipClick(entry.value) }) {
                    Text(entry.label, style = MaterialTheme.typography.labelMedium)
                    icon?.let { Icon(it, null, modifier = Modifier.size(18.dp)) }
                }
            }
            secondaryValues?.filter { it !in chipValues }?.forEach { entry ->
                NoPaddingChip(borderColor = MaterialTheme.colorScheme.primary, onClick = { onChipClick(entry.value) }) {
                    Text(entry.label, style = MaterialTheme.typography.labelMedium)
                    icon?.let { Icon(it, null, modifier = Modifier.size(18.dp)) }
                }
            }

        }

    }
}


@Composable
fun NoPaddingChip(
    borderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    color: Color = Color.Unspecified,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .border(Dp.Hairline, borderColor, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .clickable { onClick() }
            .padding(10.dp, 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            content()
        }
    }
}


object AppFilterChipDefaults {

    @Composable
    fun filterChipColors() = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
    )
}