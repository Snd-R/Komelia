package snd.komelia.ui.settings.analysis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LocalStrings
import snd.komelia.ui.common.components.Pagination
import snd.komelia.ui.platform.cursorForHand

@Composable
fun MediaAnalysisContent(
    books: List<KomeliaBook>,
    onBookClick: (KomeliaBook) -> Unit,
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Pagination(
            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (books.isEmpty()) {
            Text("Nothing to show")
        } else {
            books.forEach {
                BookAnalysisCard(
                    book = it,
                    onBookClick = onBookClick,
                    modifier = Modifier
                )
            }
        }
        Pagination(
            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun BookAnalysisCard(
    book: KomeliaBook,
    onBookClick: (KomeliaBook) -> Unit,
    modifier: Modifier
) {
    val strings = LocalStrings.current
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Text(
                book.name,
                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline),
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onBookClick(book) }
                    .cursorForHand()
            )
            SelectionContainer {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(book.url, style = MaterialTheme.typography.bodyMedium)
                    Text("${book.media.mediaType} ${book.size}")
                    val text =
                        "${book.media.status.name}: ${strings.errorCodes.getMessageForCode(book.media.comment)}"
                    Text(text, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }

}