package io.github.snd_r.komelia.strings

import io.github.snd_r.komelia.image.UpsamplingMode
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.ALLOW_ONLY
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.EXCLUDE
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.NONE
import io.github.snd_r.komelia.ui.library.LibrarySeriesTabState.SeriesSort
import io.github.snd_r.komelia.ui.library.LibrarySeriesTabState.SeriesSort.DATE_ADDED_ASC
import io.github.snd_r.komelia.ui.library.LibrarySeriesTabState.SeriesSort.DATE_ADDED_DESC
import io.github.snd_r.komelia.ui.library.LibrarySeriesTabState.SeriesSort.RELEASE_DATE_ASC
import io.github.snd_r.komelia.ui.library.LibrarySeriesTabState.SeriesSort.RELEASE_DATE_DESC
import io.github.snd_r.komelia.ui.library.LibrarySeriesTabState.SeriesSort.TITLE_ASC
import io.github.snd_r.komelia.ui.library.LibrarySeriesTabState.SeriesSort.TITLE_DESC
import io.github.snd_r.komelia.ui.library.LibrarySeriesTabState.SeriesSort.UPDATED_ASC
import io.github.snd_r.komelia.ui.library.LibrarySeriesTabState.SeriesSort.UPDATED_DESC
import io.github.snd_r.komelia.ui.reader.image.ReaderType
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType.FIT_HEIGHT
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType.FIT_WIDTH
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType.ORIGINAL
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType.SCREEN
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout.DOUBLE_PAGES
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout.DOUBLE_PAGES_NO_COVER
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.series.SeriesBooksState.BooksFilterState.BooksSort
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType
import snd.komelia.image.OnnxRuntimeUpscaleMode
import snd.komelia.image.ReduceKernel
import snd.komf.api.KomfCoreProviders
import snd.komf.api.KomfProviders
import snd.komf.api.UnknownKomfProvider
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.book.KomgaReadStatus.IN_PROGRESS
import snd.komga.client.book.KomgaReadStatus.READ
import snd.komga.client.book.KomgaReadStatus.UNREAD
import snd.komga.client.common.KomgaReadingDirection
import snd.komga.client.common.KomgaReadingDirection.LEFT_TO_RIGHT
import snd.komga.client.common.KomgaReadingDirection.RIGHT_TO_LEFT
import snd.komga.client.common.KomgaReadingDirection.VERTICAL
import snd.komga.client.common.KomgaReadingDirection.WEBTOON
import snd.komga.client.library.ScanInterval
import snd.komga.client.library.ScanInterval.DAILY
import snd.komga.client.library.ScanInterval.DISABLED
import snd.komga.client.library.ScanInterval.EVERY_12H
import snd.komga.client.library.ScanInterval.EVERY_6H
import snd.komga.client.library.ScanInterval.HOURLY
import snd.komga.client.library.ScanInterval.WEEKLY
import snd.komga.client.library.SeriesCover
import snd.komga.client.library.SeriesCover.FIRST
import snd.komga.client.library.SeriesCover.FIRST_UNREAD_OR_FIRST
import snd.komga.client.library.SeriesCover.FIRST_UNREAD_OR_LAST
import snd.komga.client.library.SeriesCover.LAST
import snd.komga.client.series.KomgaSeriesStatus
import snd.komga.client.series.KomgaSeriesStatus.ABANDONED
import snd.komga.client.series.KomgaSeriesStatus.ENDED
import snd.komga.client.series.KomgaSeriesStatus.HIATUS
import snd.komga.client.series.KomgaSeriesStatus.ONGOING
import snd.komga.client.settings.KomgaThumbnailSize
import snd.komga.client.settings.KomgaThumbnailSize.DEFAULT
import snd.komga.client.settings.KomgaThumbnailSize.LARGE
import snd.komga.client.settings.KomgaThumbnailSize.MEDIUM
import snd.komga.client.settings.KomgaThumbnailSize.XLARGE

data class AppStrings(
    val seriesView: SeriesViewStrings,
    val filters: FilterStrings,
    val seriesFilter: SeriesFilterStrings,
    val booksFilter: BookFilterStrings,
    val seriesEdit: SeriesEditStrings,
    val bookEdit: BookEditStrings,
    val libraryEdit: LibraryEditStrings,
    val userEdit: UserEditStrings,
    val reader: ReaderStrings,
    val pagedReader: PagedReaderStrings,
    val continuousReader: ContinuousReaderStrings,
    val settings: SettingsStrings,
    val imageSettings: ImageSettingsStrings,
    val errorCodes: ErrorCodes,
    val komf: KomfStrings
)

data class KomfStrings(
    val providerSettings: KomfProviderSettingsStrings
)

data class KomfProviderSettingsStrings(
    val providerAniList: String,
    val providerBangumi: String,
    val providerBookWalker: String,
    val providerComicVine: String,
    val providerHentag: String,
    val providerKodansha: String,
    val providerMal: String,
    val providerMangaBaka: String,
    val providerMangaUpdates: String,
    val providerMangaDex: String,
    val providerNautiljon: String,
    val providerYenPress: String,
    val providerViz: String,
    val providerWebtoons: String,

    ) {

    fun forProvider(provider: KomfProviders) =
        when (provider) {
            KomfCoreProviders.ANILIST -> providerAniList
            KomfCoreProviders.BANGUMI -> providerBangumi
            KomfCoreProviders.BOOK_WALKER -> providerBookWalker
            KomfCoreProviders.COMIC_VINE -> providerComicVine
            KomfCoreProviders.HENTAG -> providerHentag
            KomfCoreProviders.KODANSHA -> providerKodansha
            KomfCoreProviders.MAL -> providerMal
            KomfCoreProviders.MANGA_UPDATES -> providerMangaUpdates
            KomfCoreProviders.MANGADEX -> providerMangaDex
            KomfCoreProviders.NAUTILJON -> providerNautiljon
            KomfCoreProviders.YEN_PRESS -> providerYenPress
            KomfCoreProviders.VIZ -> providerViz
            KomfCoreProviders.MANGA_BAKA -> providerMangaBaka
            KomfCoreProviders.WEBTOONS -> providerWebtoons
            is UnknownKomfProvider -> provider.name
        }
}


data class SeriesViewStrings(
    val statusEnded: String,
    val statusOngoing: String,
    val statusAbandoned: String,
    val statusHiatus: String,

    val readingDirectionLeftToRight: String,
    val readingDirectionRightToLeft: String,
    val readingDirectionVertical: String,
    val readingDirectionWebtoon: String,
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

data class BookEditStrings(
    val title: String,
    val number: String,
    val sortNumber: String,
    val summary: String,
    val releaseDate: String,
    val isbn: String,
) {
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

data class ReaderStrings(
    val zoom: String,
    val readerPaged: String,
    val readerType: String,
    val readerContinuous: String,
    val stretchToFit: String,
    val decoder: String,
    val pagesInfo: String,
    val pageNumber: String,
    val memoryUsage: String,
    val pageDisplaySize: String,
    val pageOriginalSize: String,
) {
    fun forReaderType(type: ReaderType): String {
        return when (type) {
            ReaderType.PAGED -> readerPaged
            ReaderType.CONTINUOUS -> readerContinuous
        }
    }

}

data class PagedReaderStrings(
    val scaleType: String,
    val scaleScreen: String,
    val scaleFitWidth: String,
    val scaleFitHeight: String,
    val scaleOriginal: String,

    val readingDirection: String,
    val readingDirectionLeftToRight: String,
    val readingDirectionRightToLeft: String,

    val layout: String,
    val layoutSinglePage: String,
    val layoutDoublePages: String,
    val layoutDoublePagesNoCover: String,
    val offsetPages: String,
) {
    fun forScaleType(type: LayoutScaleType): String {
        return when (type) {
            SCREEN -> scaleScreen
            FIT_WIDTH -> scaleFitWidth
            FIT_HEIGHT -> scaleFitHeight
            ORIGINAL -> scaleOriginal
        }
    }

    fun forReadingDirection(direction: PagedReaderState.ReadingDirection): String {
        return when (direction) {
            PagedReaderState.ReadingDirection.LEFT_TO_RIGHT -> readingDirectionLeftToRight
            PagedReaderState.ReadingDirection.RIGHT_TO_LEFT -> readingDirectionRightToLeft
        }
    }

    fun forLayout(layout: PageDisplayLayout): String {
        return when (layout) {
            SINGLE_PAGE -> layoutSinglePage
            DOUBLE_PAGES -> layoutDoublePages
            DOUBLE_PAGES_NO_COVER -> layoutDoublePagesNoCover
        }
    }
}

data class ContinuousReaderStrings(
    val sidePadding: String,
    val pageSpacing: String,

    val readingDirection: String,
    val readingDirectionTopToBottom: String,
    val readingDirectionLeftToRight: String,
    val readingDirectionRightToLeft: String,
) {

    fun forReadingDirection(direction: ContinuousReaderState.ReadingDirection): String {
        return when (direction) {
            ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM -> readingDirectionTopToBottom
            ContinuousReaderState.ReadingDirection.LEFT_TO_RIGHT -> readingDirectionLeftToRight
            ContinuousReaderState.ReadingDirection.RIGHT_TO_LEFT -> readingDirectionRightToLeft
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

    val appTheme: String,
    val appThemeDark: String,
    val appThemeLight: String,
    val appThemeOled: String,
    val imageCardSize: String,
    val decoder: String,

    val epubReaderTypeKomga: String,
    val epubReaderTypeTtsu: String,
) {
    fun forThumbnailSize(size: KomgaThumbnailSize): String {
        return when (size) {
            DEFAULT -> thumbnailSizeDefault
            MEDIUM -> thumbnailSizeMedium
            LARGE -> thumbnailSizeLarge
            XLARGE -> thumbnailSizeXLarge
        }
    }

    fun forAppTheme(theme: AppTheme): String {
        return when (theme) {
            AppTheme.DARK -> appThemeDark
            AppTheme.LIGHT -> appThemeLight
            AppTheme.DARKER -> appThemeOled
        }
    }

    fun forEpubReaderType(readerType: EpubReaderType): String {
        return when (readerType) {
            EpubReaderType.KOMGA_EPUB -> epubReaderTypeKomga
            EpubReaderType.TTSU_EPUB -> epubReaderTypeTtsu
        }
    }
}

data class FilterStrings(
    val anyValue: String,

    val filterTagsSearch: String,
    val filterTagsReset: String,
    val filterTagsGenreLabel: String,
    val filterTagsTagsLabel: String,
    val filterTagsShowMore: String,
    val filterTagsShowLess: String,
)

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

    val filterTagsLabel: String,

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
    val authors: String,
    val publisher: String,
    val language: String,
    val releaseDate: String,
    val ageRating: String,
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

data class BookFilterStrings(
    val sort: String,
    val sortNumberAsc: String,
    val sortNumberDesc: String,
    val sortFileNameAsc: String,
    val sortFileNameDesc: String,
    val sortReleaseDateAsc: String,
    val sortReleaseDateDesc: String,

    val readStatus: String,
    val readStatusUnread: String,
    val readStatusInProgress: String,
    val readStatusRead: String,

    val authors: String,
    val tags: String,
) {

    fun forReadStatus(status: KomgaReadStatus): String {
        return when (status) {
            UNREAD -> readStatusUnread
            IN_PROGRESS -> readStatusInProgress
            READ -> readStatusRead
        }
    }

    fun forBookSort(sort: BooksSort): String {
        return when (sort) {
            BooksSort.NUMBER_ASC -> sortNumberAsc
            BooksSort.NUMBER_DESC -> sortNumberDesc
//            BooksSort.FILENAME_ASC -> sortFileNameAsc
//            BooksSort.FILENAME_DESC -> sortFileNameDesc
//            BooksSort.RELEASE_DATE_ASC -> sortReleaseDateAsc
//            BooksSort.RELEASE_DATE_DESC -> sortReleaseDateDesc
        }

    }
}


data class ErrorCodes(
    val err1000: String,
    val err1001: String,
    val err1002: String,
    val err1003: String,
    val err1004: String,
    val err1005: String,
    val err1006: String,
    val err1007: String,
    val err1008: String,
    val err1009: String,
    val err1015: String,
    val err1016: String,
    val err1017: String,
    val err1018: String,
    val err1019: String,
    val err1020: String,
    val err1021: String,
    val err1022: String,
    val err1023: String,
    val err1024: String,
    val err1025: String,
    val err1026: String,
    val err1027: String,
    val err1028: String,
    val err1029: String,
    val err1030: String,
    val err1031: String,
    val err1032: String,
    val err1033: String,
) {
    private val codeMap: Map<String, String> = mapOf(
        "ERR_1000" to err1000,
        "ERR_1001" to err1001,
        "ERR_1002" to err1002,
        "ERR_1003" to err1003,
        "ERR_1004" to err1004,
        "ERR_1005" to err1005,
        "ERR_1006" to err1006,
        "ERR_1007" to err1007,
        "ERR_1008" to err1008,
        "ERR_1009" to err1009,
        "ERR_1015" to err1015,
        "ERR_1016" to err1016,
        "ERR_1017" to err1017,
        "ERR_1018" to err1018,
        "ERR_1019" to err1019,
        "ERR_1020" to err1020,
        "ERR_1021" to err1021,
        "ERR_1022" to err1022,
        "ERR_1023" to err1023,
        "ERR_1024" to err1024,
        "ERR_1025" to err1025,
        "ERR_1026" to err1026,
        "ERR_1027" to err1027,
        "ERR_1028" to err1028,
        "ERR_1029" to err1029,
        "ERR_1030" to err1030,
        "ERR_1031" to err1031,
        "ERR_1032" to err1032,
        "ERR_1033" to err1033,
    )

    fun getMessageForCode(code: String) = requireNotNull(codeMap[code])
}

data class ImageSettingsStrings(
    val upsamplingMode: String,
    val upsamplingModeNearest: String,
    val upsamplingModeBilinear: String,
    val upsamplingModeMitchell: String,
    val upsamplingModeCatmullRom: String,

    val downsamplingKernel: String,
    val downsamplingKernelNearest: String,
    val downsamplingKernelLinear: String,
    val downsamplingKernelCubic: String,
    val downsamplingKernelMitchell: String,
    val downsamplingKernelLanczos2: String,
    val downsamplingKernelLanczos3: String,
    val downsamplingKernelMKS2013: String,
    val downsamplingKernelMKS2021: String,
    val downsamplingKernelDefault: String,

    val onnxRuntimeExecutionProvider: String,
    val onnxRuntimeUpscaleMode: String,
    val onnxRuntimeUpscaleModeNone: String,
    val onnxRuntimeUpscaleModeUserModel: String,
    val onnxRuntimeUpscaleModeMangaJaNai: String,
) {
    fun forUpsamplingMode(mode: UpsamplingMode): String {
        return when (mode) {
            UpsamplingMode.NEAREST -> upsamplingModeNearest
            UpsamplingMode.BILINEAR -> upsamplingModeBilinear
            UpsamplingMode.MITCHELL -> upsamplingModeMitchell
            UpsamplingMode.CATMULL_ROM -> upsamplingModeCatmullRom
        }
    }

    fun forDownsamplingKernel(kernel: ReduceKernel): String {
        return when (kernel) {
            ReduceKernel.NEAREST -> downsamplingKernelNearest
            ReduceKernel.LINEAR -> downsamplingKernelLinear
            ReduceKernel.CUBIC -> downsamplingKernelCubic
            ReduceKernel.MITCHELL -> downsamplingKernelMitchell
            ReduceKernel.LANCZOS2 -> downsamplingKernelLanczos2
            ReduceKernel.LANCZOS3 -> downsamplingKernelLanczos3
            ReduceKernel.MKS2013 -> downsamplingKernelMKS2013
            ReduceKernel.MKS2021 -> downsamplingKernelMKS2021
            ReduceKernel.DEFAULT -> downsamplingKernelDefault
        }
    }

    fun forOnnxRuntimeUpscaleMode(mode: OnnxRuntimeUpscaleMode):String{
        return when (mode) {
            OnnxRuntimeUpscaleMode.USER_SPECIFIED_MODEL -> onnxRuntimeUpscaleModeUserModel
            OnnxRuntimeUpscaleMode.MANGAJANAI_PRESET -> onnxRuntimeUpscaleModeMangaJaNai
            OnnxRuntimeUpscaleMode.NONE -> onnxRuntimeUpscaleModeNone
        }
    }
}