package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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

        val buttonsRange = (currentPage - 2).coerceAtLeast(2)..(currentPage + 2).coerceAtMost(totalPages - 1)
        if (buttonsRange.first > 2) {
            Text("...", Modifier.width(40.dp))
        }
        for (pageNumber in buttonsRange) {
            PageNumberButton(pageNumber, currentPage, onPageChange)
        }
        if (buttonsRange.last < totalPages - 1) {
            Text("...", Modifier.width(40.dp))
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

@Composable
fun PaginationWithSizeOptions(
    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    navigationButtons: Boolean = true,

    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,

    paginationModifier: Modifier = Modifier,
    spacer: @Composable (() -> Unit)? = null
) {
    Pagination(
        totalPages = totalPages,
        currentPage = currentPage,
        onPageChange = onPageChange,
        navigationButtons = navigationButtons,
        modifier = paginationModifier
    )

    spacer?.let { it() }

    DropdownChoiceMenu(
        selectedOption = intEntry(pageSize),
        options = listOf(
            intEntry(20),
            intEntry(50),
            intEntry(100),
            intEntry(200),
            intEntry(500),
        ),
        onOptionChange = { entry -> onPageSizeChange(entry.value) },
        contentPadding = PaddingValues(5.dp),
        textFieldModifier = Modifier
            .widthIn(min = 70.dp)
            .clip(RoundedCornerShape(5.dp))
            .padding(end = 10.dp)
    )
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
