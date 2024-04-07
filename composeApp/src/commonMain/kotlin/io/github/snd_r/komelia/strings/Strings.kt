package io.github.snd_r.komelia.strings

import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.ALLOW_ONLY
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.EXCLUDE
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.NONE
import io.github.snd_r.komelia.ui.reader.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.LayoutScaleType.FIT_HEIGHT
import io.github.snd_r.komelia.ui.reader.LayoutScaleType.FIT_WIDTH
import io.github.snd_r.komelia.ui.reader.LayoutScaleType.ORIGINAL
import io.github.snd_r.komelia.ui.reader.LayoutScaleType.SCREEN
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout.DOUBLE_PAGES
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import io.github.snd_r.komelia.ui.series.SeriesListViewModel.SeriesSort
import io.github.snd_r.komelia.ui.series.SeriesListViewModel.SeriesSort.DATE_ADDED_ASC
import io.github.snd_r.komelia.ui.series.SeriesListViewModel.SeriesSort.DATE_ADDED_DESC
import io.github.snd_r.komelia.ui.series.SeriesListViewModel.SeriesSort.RELEASE_DATE_ASC
import io.github.snd_r.komelia.ui.series.SeriesListViewModel.SeriesSort.RELEASE_DATE_DESC
import io.github.snd_r.komelia.ui.series.SeriesListViewModel.SeriesSort.TITLE_ASC
import io.github.snd_r.komelia.ui.series.SeriesListViewModel.SeriesSort.TITLE_DESC
import io.github.snd_r.komelia.ui.series.SeriesListViewModel.SeriesSort.UPDATED_ASC
import io.github.snd_r.komelia.ui.series.SeriesListViewModel.SeriesSort.UPDATED_DESC
import io.github.snd_r.komga.book.KomgaReadStatus
import io.github.snd_r.komga.book.KomgaReadStatus.IN_PROGRESS
import io.github.snd_r.komga.book.KomgaReadStatus.READ
import io.github.snd_r.komga.book.KomgaReadStatus.UNREAD
import io.github.snd_r.komga.common.KomgaReadingDirection
import io.github.snd_r.komga.common.KomgaReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komga.common.KomgaReadingDirection.RIGHT_TO_LEFT
import io.github.snd_r.komga.common.KomgaReadingDirection.VERTICAL
import io.github.snd_r.komga.common.KomgaReadingDirection.WEBTOON
import io.github.snd_r.komga.library.ScanInterval
import io.github.snd_r.komga.library.ScanInterval.DAILY
import io.github.snd_r.komga.library.ScanInterval.DISABLED
import io.github.snd_r.komga.library.ScanInterval.EVERY_12H
import io.github.snd_r.komga.library.ScanInterval.EVERY_6H
import io.github.snd_r.komga.library.ScanInterval.HOURLY
import io.github.snd_r.komga.library.ScanInterval.WEEKLY
import io.github.snd_r.komga.library.SeriesCover
import io.github.snd_r.komga.library.SeriesCover.FIRST
import io.github.snd_r.komga.library.SeriesCover.FIRST_UNREAD_OR_FIRST
import io.github.snd_r.komga.library.SeriesCover.FIRST_UNREAD_OR_LAST
import io.github.snd_r.komga.library.SeriesCover.LAST
import io.github.snd_r.komga.series.KomgaSeriesStatus
import io.github.snd_r.komga.series.KomgaSeriesStatus.ABANDONED
import io.github.snd_r.komga.series.KomgaSeriesStatus.ENDED
import io.github.snd_r.komga.series.KomgaSeriesStatus.HIATUS
import io.github.snd_r.komga.series.KomgaSeriesStatus.ONGOING
import io.github.snd_r.komga.settings.KomgaThumbnailSize
import io.github.snd_r.komga.settings.KomgaThumbnailSize.DEFAULT
import io.github.snd_r.komga.settings.KomgaThumbnailSize.LARGE
import io.github.snd_r.komga.settings.KomgaThumbnailSize.MEDIUM
import io.github.snd_r.komga.settings.KomgaThumbnailSize.XLARGE

data class Strings(
    val seriesFilter: SeriesFilterStrings,
    val seriesEdit: SeriesEditStrings,
    val libraryEdit: LibraryEditStrings,
    val userEdit: UserEditStrings,
    val readerSettings: ReaderSettingsStrings,
    val settings: SettingsStrings,
)

data class SeriesEditStrings(
    val title: String,
    val sortTitle: String,
    val summary: String,
    val language: String,

    val status: String,
    val statusEnded: String,
    val statusOngoing: String,
    val statusAbandoned: String,
    val statusHiatus: String,

    val readingDirection: String,
    val readingDirectionLeftToRight: String,
    val readingDirectionRightToLeft: String,
    val readingDirectionVertical: String,
    val readingDirectionWebtoon: String,

    val publisher: String,
    val ageRating: String,
    val totalBookCount: String,
) {

    fun forSeriesStatus(status: KomgaSeriesStatus): String {
        return when (status) {
            ENDED -> statusEnded
            ONGOING -> statusOngoing
            ABANDONED -> statusAbandoned
            HIATUS -> statusHiatus
        }
    }

    fun forReadingDirection(direction: KomgaReadingDirection): String {
        return when (direction) {
            LEFT_TO_RIGHT -> readingDirectionLeftToRight
            RIGHT_TO_LEFT -> readingDirectionRightToLeft
            VERTICAL -> readingDirectionVertical
            WEBTOON -> readingDirectionWebtoon
        }
    }
}


data class LibraryEditStrings(
    val emptyTrashAfterScan: String,
    val scanForceModifiedTime: String,
    val scanOnStartup: String,
    val oneshotsDirectory: String,
    val excludeDirectories: String,
    val scanInterval: String,
    val scanIntervalDisabled: String,
    val scanIntervalHourly: String,
    val scanIntervalEvery6H: String,
    val scanIntervalEvery12H: String,
    val scanIntervalDaily: String,
    val scanIntervalWeekly: String,


    val hashFiles: String,
    val hashPages: String,
    val analyzeDimensions: String,
    val repairExtensions: String,
    val convertToCbz: String,
    val seriesCover: String,

    val coverFirst: String,
    val coverFirstUnreadOrFirst: String,
    val coverFirstUnreadOrLast: String,
    val coverLast: String,
) {

    fun forSeriesCover(cover: SeriesCover): String {
        return when (cover) {
            FIRST -> coverFirst
            FIRST_UNREAD_OR_FIRST -> coverFirstUnreadOrFirst
            FIRST_UNREAD_OR_LAST -> coverFirstUnreadOrLast
            LAST -> coverLast
        }
    }

    fun forScanInterval(scanInterval: ScanInterval): String {
        return when (scanInterval) {
            DISABLED -> scanIntervalDisabled
            HOURLY -> scanIntervalHourly
            EVERY_6H -> scanIntervalEvery6H
            EVERY_12H -> scanIntervalEvery12H
            DAILY -> scanIntervalDaily
            WEEKLY -> scanIntervalWeekly
        }
    }
}

data class UserEditStrings(
    val contentRestrictions: String,
    val age: String,
    val labelsAllow: String,
    val labelsExclude: String,
    val ageRestriction: String,
    val ageRestrictionNone: String,
    val ageRestrictionAllowOnly: String,
    val ageRestrictionExclude: String,
) {
    fun forAgeRestriction(ageRestriction: UserEditDialogViewModel.AgeRestriction): String {
        return when (ageRestriction) {
            NONE -> ageRestrictionNone
            ALLOW_ONLY -> ageRestrictionAllowOnly
            EXCLUDE -> ageRestrictionExclude
        }
    }
}

data class ReaderSettingsStrings(
    val scaleType: String,
    val scaleScreen: String,
    val scaleFitWidth: String,
    val scaleFitHeight: String,
    val scaleOriginal: String,
    val upsample: String,
    val readingDirection: String,
    val readingDirectionLeftToRight: String,
    val readingDirectionRightToLeft: String,
    val layout: String,
    val layoutSinglePage: String,
    val layoutDoublePages: String,
    val offsetPages: String,
    val decoder: String,
    val pageNumber: String,
    val memoryUsage: String,
    val pageScaledSize: String,
    val pageOriginalSize: String,
    val noPageDimensionsWarning: String,
) {
    fun forScaleType(type: LayoutScaleType): String {
        return when (type) {
            SCREEN -> scaleScreen
            FIT_WIDTH -> scaleFitWidth
            FIT_HEIGHT -> scaleFitHeight
            ORIGINAL -> scaleOriginal
        }
    }

    fun forReadingDirection(direction: ReadingDirection): String {
        return when (direction) {
            ReadingDirection.LEFT_TO_RIGHT -> readingDirectionLeftToRight
            ReadingDirection.RIGHT_TO_LEFT -> readingDirectionRightToLeft
        }
    }

    fun forLayout(layout: PageDisplayLayout): String {
        return when (layout) {
            SINGLE_PAGE -> layoutSinglePage
            DOUBLE_PAGES -> layoutDoublePages
        }
    }

}

data class SettingsStrings(
    val serverSettings: String,
    val thumbnailSize: String,
    val thumbnailSizeDefault: String,
    val thumbnailSizeMedium: String,
    val thumbnailSizeLarge: String,
    val thumbnailSizeXLarge: String,

    val thumbnailRegenTitle: String,
    val thumbnailRegenBody: String,
    val thumbnailRegenIfBigger: String,
    val thumbnailRegenAllBooks: String,
    val thumbnailRegenNo: String,


    val deleteEmptyCollections: String,
    val deleteEmptyReadLists: String,
    val taskPoolSize: String,
    val rememberMeDurationDays: String,
    val renewRememberMeKey: String,
    val serverPort: String,
    val serverContextPath: String,
    val requiresRestart: String,
    val serverSettingsDiscard: String,
    val serverSettingsSave: String,

    val imageCardSize: String,
    val decoder: String
) {
    fun forThumbnailSize(size: KomgaThumbnailSize): String {
        return when (size) {
            DEFAULT -> thumbnailSizeDefault
            MEDIUM -> thumbnailSizeMedium
            LARGE -> thumbnailSizeLarge
            XLARGE -> thumbnailSizeXLarge
        }
    }
}

data class SeriesFilterStrings(
    val resetFilters: String,
    val hideFilters: String,
    val anyValue: String,
    val search: String,
    val sort: String,
    val sortTitleAsc: String,
    val sortTitleDesc: String,
    val sortDateAddedAsc: String,
    val sortDateAddedDesc: String,
    val sortReleaseDateAsc: String,
    val sortReleaseDateDesc: String,
    val sortUpdatedAsc: String,
    val sortUpdatedDesc: String,
    val sortFolderNameAsc: String,
    val sortFolderNameDesc: String,
    val sortBooksCountAsc: String,
    val sortBooksCountDesc: String,

    val filterTags: String,
    val filterTagsSearch: String,
    val filterTagsReset: String,
    val filterTagsGenreLabel: String,
    val filterTagsTagsLabel: String,
    val filterTagsShowMore: String,
    val filterTagsShowLess: String,

    val readStatus: String,
    val readStatusUnread: String,
    val readStatusInProgress: String,
    val readStatusRead: String,

    val publicationStatus: String,
    val pubStatusEnded: String,
    val pubStatusOngoing: String,
    val pubStatusAbandoned: String,
    val pubStatusHiatus: String,

    val complete: String,
    val oneshot: String,
    val authors:String,
    val publisher:String,
    val language:String,
    val releaseDate:String,
    val ageRating:String,
) {

    fun forSeriesSort(sort: SeriesSort): String {
        return when (sort) {
            TITLE_ASC -> sortTitleAsc
            TITLE_DESC -> sortTitleDesc
            DATE_ADDED_ASC -> sortDateAddedAsc
            DATE_ADDED_DESC -> sortDateAddedDesc
            RELEASE_DATE_ASC -> sortReleaseDateAsc
            RELEASE_DATE_DESC -> sortReleaseDateDesc
            UPDATED_DESC -> sortUpdatedDesc
            UPDATED_ASC -> sortUpdatedAsc
//            FOLDER_NAME_ASC -> sortFolderNameAsc
//            FOLDER_NAME_DESC -> sortFolderNameDesc
//            BOOKS_COUNT_ASC -> sortBooksCountAsc
//            BOOKS_COUNT_DESC -> sortBooksCountDesc
        }
    }

    fun forSeriesReadStatus(status: KomgaReadStatus): String {
        return when (status) {
            UNREAD -> readStatusUnread
            IN_PROGRESS -> readStatusInProgress
            READ -> readStatusRead
        }
    }

    fun forPublicationStatus(status: KomgaSeriesStatus): String {
        return when (status) {
            ENDED -> pubStatusEnded
            ONGOING -> pubStatusOngoing
            ABANDONED -> pubStatusAbandoned
            HIATUS -> pubStatusHiatus
        }
    }

}
