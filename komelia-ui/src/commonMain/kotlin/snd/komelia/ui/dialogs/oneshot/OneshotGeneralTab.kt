package snd.komelia.ui.dialogs.oneshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import snd.komelia.ui.LocalStrings
import snd.komelia.ui.OptionsStateHolder
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.LockableDropDown
import snd.komelia.ui.common.components.LockableTextField
import snd.komelia.ui.dialogs.book.edit.BookEditMetadataState
import snd.komelia.ui.dialogs.series.edit.SeriesEditMetadataState
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komga.client.common.KomgaReadingDirection

class OneshotGeneralTab(
    private val seriesMetadata: SeriesEditMetadataState,
    private val bookMetadata: BookEditMetadataState
) : DialogTab {
    override fun options() = TabItem(
        title = "GENERAL",
        icon = Icons.Default.FormatAlignCenter
    )

    @Composable
    override fun Content() {
        GeneralTabContent(
            title = StateHolder(
                bookMetadata.title,
                {
                    bookMetadata.title = it
                    seriesMetadata.title = it
                }
            ),
            titleLock = StateHolder(
                seriesMetadata.titleLock,
                seriesMetadata::titleLock::set
            ),
            sortTitle = StateHolder(
                seriesMetadata.titleSort,
                seriesMetadata::titleSort::set
            ),
            sortTitleLock = StateHolder(
                seriesMetadata.titleSortLock,
                seriesMetadata::titleSortLock::set
            ),
            summary = StateHolder(
                bookMetadata.summary,
                bookMetadata::summary::set
            ),
            summaryLock = StateHolder(
                bookMetadata.summaryLock,
                bookMetadata::summaryLock::set
            ),
            language = StateHolder(
                seriesMetadata.language,
                seriesMetadata::language::set
            ),
            languageLock = StateHolder(
                seriesMetadata.languageLock,
                seriesMetadata::languageLock::set
            ),
            readingDirection = OptionsStateHolder(
                seriesMetadata.readingDirection,
                KomgaReadingDirection.entries,
                seriesMetadata::readingDirection::set
            ),
            readingDirectionLock = StateHolder(
                seriesMetadata.readingDirectionLock,
                seriesMetadata::readingDirectionLock::set
            ),
            publisher = StateHolder(
                seriesMetadata.publisher,
                seriesMetadata::publisher::set
            ),
            publisherLock = StateHolder(
                seriesMetadata.publisherLock,
                seriesMetadata::publisherLock::set
            ),
            ageRating = StateHolder(
                seriesMetadata.ageRating,
                seriesMetadata::ageRating::set
            ),
            ageRatingLock = StateHolder(
                seriesMetadata.ageRatingLock,
                seriesMetadata::ageRatingLock::set
            ),
            releaseDate = StateHolder(
                bookMetadata.releaseDate,
                bookMetadata::releaseDate::set
            ),
            releaseDateLock = StateHolder(
                bookMetadata.releaseDateLock,
                bookMetadata::releaseDateLock::set
            ),
            isbn = StateHolder(
                bookMetadata.isbn,
                bookMetadata::isbn::set
            ),
            isbnLock = StateHolder(
                bookMetadata.isbnLock,
                bookMetadata::isbnLock::set
            )
        )
    }

    @Composable
    private fun GeneralTabContent(
        title: StateHolder<String>,
        titleLock: StateHolder<Boolean>,
        sortTitle: StateHolder<String>,
        sortTitleLock: StateHolder<Boolean>,
        summary: StateHolder<String>,
        summaryLock: StateHolder<Boolean>,
        language: StateHolder<String>,
        languageLock: StateHolder<Boolean>,
        readingDirection: OptionsStateHolder<KomgaReadingDirection?>,
        readingDirectionLock: StateHolder<Boolean>,
        publisher: StateHolder<String>,
        publisherLock: StateHolder<Boolean>,
        ageRating: StateHolder<Int?>,
        ageRatingLock: StateHolder<Boolean>,
        releaseDate: StateHolder<String>,
        releaseDateLock: StateHolder<Boolean>,
        isbn: StateHolder<String>,
        isbnLock: StateHolder<Boolean>,
    ) {
        val seriesStrings = LocalStrings.current.seriesEdit
        val bookStrings = LocalStrings.current.bookEdit

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LockableTextField(
                text = title.value,
                onTextChange = title.setValue,
                errorMessage = title.errorMessage,
                label = bookStrings.title,
                lock = titleLock,
                modifier = Modifier.fillMaxWidth()
            )
            LockableTextField(
                text = sortTitle.value,
                onTextChange = sortTitle.setValue,
                errorMessage = sortTitle.errorMessage,
                label = seriesStrings.sortTitle,
                lock = sortTitleLock,
            )

            LockableTextField(
                text = summary.value,
                onTextChange = summary.setValue,
                errorMessage = summary.errorMessage,
                label = bookStrings.summary,
                lock = summaryLock,
                minLines = 6,
                maxLines = 12,
                modifier = Modifier.fillMaxWidth(),
                textFieldModifier = Modifier
            )

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                LockableDropDown(
                    selectedOption = readingDirection.value?.let {
                        LabeledEntry(
                            it,
                            seriesStrings.forReadingDirection(it)
                        )
                    }
                        ?: LabeledEntry(null, ""),
                    options = KomgaReadingDirection.entries.map {
                        LabeledEntry(
                            it,
                            seriesStrings.forReadingDirection(it)
                        )
                    },
                    onOptionChange = { readingDirection.onValueChange(it.value) },
                    label = { Text(seriesStrings.readingDirection) },
                    lock = readingDirectionLock,
                    inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
                    inputFieldModifier = Modifier.weight(.5f)
                )

                LockableTextField(
                    text = language.value,
                    onTextChange = language.setValue,
                    errorMessage = language.errorMessage,
                    label = seriesStrings.language,
                    lock = languageLock,
                    maxLines = 1,
                    modifier = Modifier.weight(.5f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                LockableTextField(
                    text = publisher.value,
                    onTextChange = publisher.setValue,
                    errorMessage = publisher.errorMessage,
                    label = seriesStrings.publisher,
                    lock = publisherLock,
                    maxLines = 1,
                    modifier = Modifier.weight(.5f)
                )

                LockableTextField(
                    text = ageRating.value?.toString() ?: "",
                    onTextChange = {
                        try {
                            if (it.isBlank()) ageRating.setValue(null)
                            else ageRating.setValue(it.toInt())
                        } catch (e: NumberFormatException) {
                            // ignore
                        }
                    },
                    errorMessage = ageRating.errorMessage,
                    label = seriesStrings.ageRating,
                    lock = ageRatingLock,
                    maxLines = 1,
                    modifier = Modifier.weight(.5f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                LockableTextField(
                    text = releaseDate.value,
                    onTextChange = { releaseDate.setValue(it) },
                    errorMessage = releaseDate.errorMessage,
                    label = bookStrings.releaseDate,
                    lock = releaseDateLock,
                    maxLines = 1,
                    modifier = Modifier.weight(.5f)
                )

                LockableTextField(
                    text = isbn.value,
                    onTextChange = isbn.setValue,
                    errorMessage = isbn.errorMessage,
                    label = bookStrings.isbn,
                    lock = isbnLock,
                    maxLines = 1,
                    modifier = Modifier.weight(.5f)
                )
            }
        }
    }
}