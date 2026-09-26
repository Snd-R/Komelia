package snd.komelia.ui.series.mangabaka

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.markdownPadding
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.platform.WindowSizeClass.COMPACT
import snd.komelia.ui.platform.WindowSizeClass.EXPANDED
import snd.komelia.ui.platform.WindowSizeClass.FULL
import snd.komelia.ui.platform.WindowSizeClass.MEDIUM

@Composable
fun MangaBakaDescription(description: String) {
    val windowWidth = LocalWindowWidth.current
    val overflowHeight = remember(windowWidth) {
        when (windowWidth) {
            COMPACT, MEDIUM -> 600.dp
            EXPANDED, FULL -> 400.dp
        }
    }
    val content = remember(description) { description.replace("\\-","-") }
    ExpandableBox(
        overflowHeight = overflowHeight
    ) {
        Column {
            SelectionContainer {
                Markdown(
                    content = content,
                    modifier = Modifier.widthIn(max = 1400.dp),
                    padding = markdownPadding(
                        block = 4.dp,
                        list = 2.dp,
                        listItemTop = 2.dp,
                        listItemBottom = 2.dp,
                        listIndent = 8.dp,
                    )
                )
            }
        }
    }
}
