package snd.komelia.db

import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType.SCREEN
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.PageDisplayLayout.SINGLE_PAGE

data class ImageReaderSettings(
    val readerType: ReaderType = ReaderType.PAGED,
    val stretchToFit: Boolean = true,
    val pagedScaleType: LayoutScaleType = SCREEN,
    val pagedReadingDirection: PagedReaderState.ReadingDirection = PagedReaderState.ReadingDirection.LEFT_TO_RIGHT,
    val pagedPageLayout: PageDisplayLayout = SINGLE_PAGE,
    val continuousReadingDirection: ContinuousReaderState.ReadingDirection = ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM,
    val continuousPadding: Float = .3f,
    val continuousPageSpacing: Int = 0,
    val cropBorders: Boolean = false,
)