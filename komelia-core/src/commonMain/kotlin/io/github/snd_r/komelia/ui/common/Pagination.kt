package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.intEntry

@Composable
fun Pagination(
    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    navigationButtons: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (totalPages <= 1) {
        Box(modifier)
        return
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val buttonDistance = when (maxWidth) {
            in 0.dp..500.dp -> 1
            in 0.dp..600.dp -> 2
            in 600.dp..700.dp -> 3
            in 700.dp..800.dp -> 4
            else -> 5
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (navigationButtons)
                IconButton(
                    enabled = currentPage != 1,
                    onClick = { onPageChange(currentPage - 1) },
                    modifier = Modifier.cursorForHand()
                ) {
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = null,
                    )
                }

            PageNumberButton(1, currentPage, onPageChange)

            val minValue = (currentPage - buttonDistance).coerceAtLeast(2)
            val maxValue = (currentPage + buttonDistance).coerceAtMost(totalPages - 1)
            val buttonsRange = minValue..maxValue

            if (buttonsRange.first > 2) {
                Text("...", Modifier.width(20.dp))
            }
            for (pageNumber in buttonsRange) {
                PageNumberButton(pageNumber, currentPage, onPageChange)
            }
            if (buttonsRange.last < totalPages - 1) {
                Text("...", Modifier.width(20.dp))
            }

            PageNumberButton(totalPages, currentPage, onPageChange)

            if (navigationButtons)
                IconButton(
                    enabled = currentPage != totalPages,
                    onClick = { onPageChange(currentPage + 1) },
                    modifier = Modifier.cursorForHand()
                ) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                    )
                }
        }
    }
}

@Composable
private fun PageNumberButton(
    pageNumber: Int,
    currentPage: Int,
    onClick: (Int) -> Unit
) {
    IconButton(
        enabled = pageNumber != currentPage,
        onClick = { onClick(pageNumber) },
        colors = IconButtonDefaults.iconButtonColors(
            disabledContainerColor = MaterialTheme.colorScheme.secondary,
            disabledContentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        modifier = Modifier.cursorForHand()

    ) {
        Text(pageNumber.toString())
    }
}


@Composable
fun PageSizeSelectionDropdown(
    currentSize: Int,
    onPageSizeChange: (Int) -> Unit
) {
    DropdownChoiceMenu(
        selectedOption = intEntry(currentSize),
        options = listOf(
            intEntry(20),
            intEntry(50),
            intEntry(100),
            intEntry(200),
            intEntry(500)
        ),
        onOptionChange = { onPageSizeChange(it.value) },
        contentPadding = PaddingValues(5.dp),
        inputFieldColor = MaterialTheme.colorScheme.surface,
        inputFieldModifier = Modifier
            .widthIn(min = 70.dp)
            .clip(RoundedCornerShape(5.dp))
            .padding(end = 10.dp)
    )
}