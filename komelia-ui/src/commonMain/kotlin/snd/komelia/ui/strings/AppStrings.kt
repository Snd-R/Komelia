package snd.komelia.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.filter_exclude_if_all_match
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.filter_exclude_if_any_match
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.filter_include_If_any_match
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.filter_include_if_all_match
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_anilist
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_bangumi
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_bookwalker
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_comicvine
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_hentag
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_kodansha
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_mal
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_mangabaka
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_mangadex
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_mangaupdates
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_nautiljon
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_viz
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_webtoons
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_provider_yenpress
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1000
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1001
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1002
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1003
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1004
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1005
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1006
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1007
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1008
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1009
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1015
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1016
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1017
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1018
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1019
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1020
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1021
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1022
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1023
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1024
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1025
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1026
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1027
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1028
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1029
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1030
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1031
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1032
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1033
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1034
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1035
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1036
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1037
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1038
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_1039
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komga_error_code_unknown
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_cover_first
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_cover_first_unread_or_first
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_cover_first_unread_or_last
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_cover_last
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_interval_daily
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_interval_disabled
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_interval_every_12h
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_interval_every_6h
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_interval_hourly
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_interval_weekly
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_continuous_reading_direction_left_to_rigth
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_continuous_reading_direction_right_to_left
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_continuous_reading_direction_top_to_bottom
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_downsampling_kernel_default
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_downsampling_kernel_lanczos2
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_downsampling_kernel_lanczos3
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_downsampling_kernel_mitchell
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_upsampling_mode_bicubic_catmull_rom
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_upsampling_mode_bicubic_mitchell
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_upsampling_mode_bilinear
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_upsampling_mode_nearest
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_layout_double_pages
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_layout_double_pages_no_cover
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_layout_single_page
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_reading_direction_left_to_right
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_reading_direction_right_to_left
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_scale_fit_height
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_scale_fit_width
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_scale_original
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_scale_type
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_type_continuous
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_type_paged
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_type_panels
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_book_filter_read_status_inprogress
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_book_filter_read_status_read
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_book_filter_read_status_unread
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_book_filter_sort_numbers_asc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_book_filter_sort_numbers_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_publication_status_abandoned
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_publication_status_ended
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_publication_status_hiatus
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_publication_status_ongoing
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_read_status_inprogress
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_read_status_read
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_read_status_unread
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_sort_date_added_asc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_sort_date_added_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_sort_release_date_asc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_sort_release_date_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_sort_title_asc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_sort_title_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_sort_updated_asc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_filter_sort_updated_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_reading_direction_left_to_right
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_reading_direction_vertical
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_reading_direction_webtoon
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_status_abandoned
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_status_ended
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_status_hiatus
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_status_ongoing
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_app_theme_dark
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_app_theme_darker
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_app_theme_light
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_epub_reader_type_komga
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_epub_reader_type_ttsu
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_upscale_mode_mangajanai
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_upscale_mode_none
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_upscale_mode_user_model
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_navigation_komf_connection
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_navigation_komf_jobs
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_navigation_komf_notifications
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_navigation_komf_processing
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_navigation_komf_providers
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_thumbnail_size_default
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_thumbnail_size_large
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_thumbnail_size_medium
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_thumbnail_size_xlarge
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_age_restriction_allow_only
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_age_restriction_exclude
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_age_restriction_none
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_roles_admin
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_roles_file_download
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_roles_kobo_sync
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_roles_koreader_sync
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_roles_page_streaming
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_roles_user
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.rememberResourceEnvironment
import org.jetbrains.compose.resources.stringResource
import snd.komelia.image.ReduceKernel
import snd.komelia.image.UpsamplingMode
import snd.komelia.image.UpscaleMode
import snd.komelia.settings.model.AppTheme
import snd.komelia.settings.model.ContinuousReadingDirection
import snd.komelia.settings.model.EpubReaderType
import snd.komelia.settings.model.LayoutScaleType
import snd.komelia.settings.model.PageDisplayLayout
import snd.komelia.settings.model.PagedReadingDirection
import snd.komelia.settings.model.ReaderType
import snd.komelia.ui.book.BooksFilterState.BooksSort
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.dialogs.user.UserEditDialogViewModel
import snd.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.ALLOW_ONLY
import snd.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.EXCLUDE
import snd.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction.NONE
import snd.komelia.ui.library.LibrarySeriesTabState
import snd.komelia.ui.series.SeriesFilterState
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

data object AppStrings {

    val komfConnection = Res.string.settings_navigation_komf_connection
    val komfJobs = Res.string.settings_navigation_komf_jobs
    val komfNotifications = Res.string.settings_navigation_komf_notifications
    val komfProcessing = Res.string.settings_navigation_komf_processing
    val komfProviders = Res.string.settings_navigation_komf_providers
    fun forReadStatus(status: KomgaReadStatus): StringResource {
        return when (status) {
            UNREAD -> Res.string.series_book_filter_read_status_unread
            IN_PROGRESS -> Res.string.series_book_filter_read_status_inprogress
            READ -> Res.string.series_book_filter_read_status_read
        }
    }

    fun forBookSort(sort: BooksSort): StringResource {
        return when (sort) {
            BooksSort.NUMBER_ASC -> Res.string.series_book_filter_sort_numbers_asc
            BooksSort.NUMBER_DESC -> Res.string.series_book_filter_sort_numbers_desc
        }

    }

    fun forSeriesStatus(status: KomgaSeriesStatus): StringResource {
        return when (status) {
            ENDED -> Res.string.series_status_ended
            ONGOING -> Res.string.series_status_ongoing
            ABANDONED -> Res.string.series_status_abandoned
            HIATUS -> Res.string.series_status_hiatus
        }
    }

    fun forReadingDirection(direction: KomgaReadingDirection): StringResource {
        return when (direction) {
            LEFT_TO_RIGHT -> Res.string.series_reading_direction_left_to_right
            RIGHT_TO_LEFT -> Res.string.series_reading_direction_left_to_right
            VERTICAL -> Res.string.series_reading_direction_vertical
            WEBTOON -> Res.string.series_reading_direction_webtoon
        }
    }

    fun forInclusionMode(mode: SeriesFilterState.TagInclusionMode) = when (mode) {
        SeriesFilterState.TagInclusionMode.INCLUDE_IF_ALL_MATCH -> Res.string.filter_include_if_all_match
        SeriesFilterState.TagInclusionMode.INCLUDE_IF_ANY_MATCH -> Res.string.filter_include_If_any_match
    }

    fun forExclusionMode(mode: SeriesFilterState.TagExclusionMode) = when (mode) {
        SeriesFilterState.TagExclusionMode.EXCLUDE_IF_ANY_MATCH -> Res.string.filter_exclude_if_any_match
        SeriesFilterState.TagExclusionMode.EXCLUDE_IF_ALL_MATCH -> Res.string.filter_exclude_if_all_match
    }

    fun forSeriesSort(sort: LibrarySeriesTabState.SeriesSort): StringResource {
        return when (sort) {
            LibrarySeriesTabState.SeriesSort.TITLE_ASC -> Res.string.series_filter_sort_title_asc
            LibrarySeriesTabState.SeriesSort.TITLE_DESC -> Res.string.series_filter_sort_title_desc
            LibrarySeriesTabState.SeriesSort.DATE_ADDED_ASC -> Res.string.series_filter_sort_date_added_asc
            LibrarySeriesTabState.SeriesSort.DATE_ADDED_DESC -> Res.string.series_filter_sort_date_added_desc
            LibrarySeriesTabState.SeriesSort.RELEASE_DATE_ASC -> Res.string.series_filter_sort_release_date_asc
            LibrarySeriesTabState.SeriesSort.RELEASE_DATE_DESC -> Res.string.series_filter_sort_release_date_desc
            LibrarySeriesTabState.SeriesSort.UPDATED_DESC -> Res.string.series_filter_sort_updated_asc
            LibrarySeriesTabState.SeriesSort.UPDATED_ASC -> Res.string.series_filter_sort_updated_desc
        }
    }

    fun forSeriesReadStatus(status: KomgaReadStatus): StringResource {
        return when (status) {
            UNREAD -> Res.string.series_filter_read_status_unread
            IN_PROGRESS -> Res.string.series_filter_read_status_inprogress
            READ -> Res.string.series_filter_read_status_read
        }
    }

    fun forPublicationStatus(status: KomgaSeriesStatus): StringResource {
        return when (status) {
            ENDED -> Res.string.series_filter_publication_status_ended
            ONGOING -> Res.string.series_filter_publication_status_ongoing
            ABANDONED -> Res.string.series_filter_publication_status_abandoned
            HIATUS -> Res.string.series_filter_publication_status_hiatus
        }
    }

    fun forSeriesCover(cover: SeriesCover): StringResource {
        return when (cover) {
            FIRST -> Res.string.library_edit_cover_first
            FIRST_UNREAD_OR_FIRST -> Res.string.library_edit_cover_first_unread_or_first
            FIRST_UNREAD_OR_LAST -> Res.string.library_edit_cover_first_unread_or_last
            LAST -> Res.string.library_edit_cover_last
        }
    }

    fun forScanInterval(scanInterval: ScanInterval): StringResource {
        return when (scanInterval) {
            DISABLED -> Res.string.library_edit_scan_interval_disabled
            HOURLY -> Res.string.library_edit_scan_interval_hourly
            EVERY_6H -> Res.string.library_edit_scan_interval_every_6h
            EVERY_12H -> Res.string.library_edit_scan_interval_every_12h
            DAILY -> Res.string.library_edit_scan_interval_daily
            WEEKLY -> Res.string.library_edit_scan_interval_weekly
        }
    }

    fun forAgeRestriction(ageRestriction: UserEditDialogViewModel.AgeRestriction): StringResource {
        return when (ageRestriction) {
            NONE -> Res.string.user_edit_age_restriction_none
            ALLOW_ONLY -> Res.string.user_edit_age_restriction_allow_only
            EXCLUDE -> Res.string.user_edit_age_restriction_exclude
        }
    }

    fun forReaderType(type: ReaderType): StringResource {
        return when (type) {
            ReaderType.PAGED -> Res.string.reader_type_paged
            ReaderType.PANELS -> Res.string.reader_type_panels
            ReaderType.CONTINUOUS -> Res.string.reader_type_continuous
        }
    }

    fun forScaleType(type: LayoutScaleType): StringResource {
        return when (type) {
            LayoutScaleType.SCREEN -> Res.string.reader_paged_scale_type
            LayoutScaleType.FIT_WIDTH -> Res.string.reader_paged_scale_fit_width
            LayoutScaleType.FIT_HEIGHT -> Res.string.reader_paged_scale_fit_height
            LayoutScaleType.ORIGINAL -> Res.string.reader_paged_scale_original
        }
    }

    fun forReadingDirection(direction: PagedReadingDirection): StringResource {
        return when (direction) {
            PagedReadingDirection.LEFT_TO_RIGHT -> Res.string.reader_paged_reading_direction_left_to_right
            PagedReadingDirection.RIGHT_TO_LEFT -> Res.string.reader_paged_reading_direction_right_to_left
        }
    }

    fun forLayout(layout: PageDisplayLayout): StringResource {
        return when (layout) {
            PageDisplayLayout.SINGLE_PAGE -> Res.string.reader_paged_layout_single_page
            PageDisplayLayout.DOUBLE_PAGES -> Res.string.reader_paged_layout_double_pages
            PageDisplayLayout.DOUBLE_PAGES_NO_COVER -> Res.string.reader_paged_layout_double_pages_no_cover
        }
    }

    fun forReadingDirection(direction: ContinuousReadingDirection): StringResource {
        return when (direction) {
            ContinuousReadingDirection.TOP_TO_BOTTOM -> Res.string.reader_continuous_reading_direction_top_to_bottom
            ContinuousReadingDirection.LEFT_TO_RIGHT -> Res.string.reader_continuous_reading_direction_left_to_rigth
            ContinuousReadingDirection.RIGHT_TO_LEFT -> Res.string.reader_continuous_reading_direction_right_to_left
        }
    }

    fun forUpsamplingMode(mode: UpsamplingMode): StringResource {
        return when (mode) {
            UpsamplingMode.NEAREST -> Res.string.reader_image_upsampling_mode_nearest
            UpsamplingMode.BILINEAR -> Res.string.reader_image_upsampling_mode_bilinear
            UpsamplingMode.MITCHELL -> Res.string.reader_image_upsampling_mode_bicubic_mitchell
            UpsamplingMode.CATMULL_ROM -> Res.string.reader_image_upsampling_mode_bicubic_catmull_rom
        }
    }

    fun forDownsamplingKernel(kernel: ReduceKernel): StringResource {
        return when (kernel) {
            ReduceKernel.NEAREST -> Res.string.reader_image_downsampling_kernel_default
            ReduceKernel.LINEAR -> Res.string.reader_image_downsampling_kernel_default
            ReduceKernel.CUBIC -> Res.string.reader_image_downsampling_kernel_default
            ReduceKernel.MITCHELL -> Res.string.reader_image_downsampling_kernel_mitchell
            ReduceKernel.LANCZOS2 -> Res.string.reader_image_downsampling_kernel_lanczos2
            ReduceKernel.LANCZOS3 -> Res.string.reader_image_downsampling_kernel_lanczos3
            ReduceKernel.MKS2013 -> Res.string.reader_image_downsampling_kernel_default
            ReduceKernel.MKS2021 -> Res.string.reader_image_downsampling_kernel_default
            ReduceKernel.DEFAULT -> Res.string.reader_image_downsampling_kernel_default
        }
    }

    fun forOnnxRuntimeUpscaleMode(mode: UpscaleMode): StringResource {
        return when (mode) {
            UpscaleMode.USER_SPECIFIED_MODEL -> Res.string.settings_image_onnxruntime_upscale_mode_user_model
            UpscaleMode.MANGAJANAI_PRESET -> Res.string.settings_image_onnxruntime_upscale_mode_mangajanai
            UpscaleMode.NONE -> Res.string.settings_image_onnxruntime_upscale_mode_none
        }
    }

    fun forThumbnailSize(size: KomgaThumbnailSize): StringResource {
        return when (size) {
            DEFAULT -> Res.string.settings_server_thumbnail_size_default
            MEDIUM -> Res.string.settings_server_thumbnail_size_medium
            LARGE -> Res.string.settings_server_thumbnail_size_large
            XLARGE -> Res.string.settings_server_thumbnail_size_xlarge
        }
    }

    fun forAppTheme(theme: AppTheme): StringResource {
        return when (theme) {
            AppTheme.DARK -> Res.string.settings_app_theme_dark
            AppTheme.LIGHT -> Res.string.settings_app_theme_light
            AppTheme.DARKER -> Res.string.settings_app_theme_darker
        }
    }

    fun forEpubReaderType(readerType: EpubReaderType): StringResource {
        return when (readerType) {
            EpubReaderType.KOMGA_EPUB -> Res.string.settings_epub_reader_type_komga
            EpubReaderType.TTSU_EPUB -> Res.string.settings_epub_reader_type_ttsu
        }
    }

    @Composable
    fun forUserRole(roleString: String): String {
        return when (roleString) {
            "ADMIN" -> stringResource(Res.string.user_roles_admin)
            "USER" -> stringResource(Res.string.user_roles_user)
            "FILE_DOWNLOAD" -> stringResource(Res.string.user_roles_file_download)
            "PAGE_STREAMING" -> stringResource(Res.string.user_roles_page_streaming)
            "KOBO_SYNC" -> stringResource(Res.string.user_roles_kobo_sync)
            "KOREADER_SYNC" -> stringResource(Res.string.user_roles_koreader_sync)
            else -> roleString
        }
    }

    @Composable
    fun forProvider(provider: KomfProviders): String =
        when (provider) {
            KomfCoreProviders.ANILIST -> stringResource(Res.string.komf_provider_anilist)
            KomfCoreProviders.BANGUMI -> stringResource(Res.string.komf_provider_bangumi)

            KomfCoreProviders.BOOK_WALKER -> stringResource(Res.string.komf_provider_bookwalker)
            KomfCoreProviders.COMIC_VINE -> stringResource(Res.string.komf_provider_comicvine)
            KomfCoreProviders.HENTAG -> stringResource(Res.string.komf_provider_hentag)
            KomfCoreProviders.KODANSHA -> stringResource(Res.string.komf_provider_kodansha)
            KomfCoreProviders.MAL -> stringResource(Res.string.komf_provider_mal)
            KomfCoreProviders.MANGA_UPDATES -> stringResource(Res.string.komf_provider_mangaupdates)
            KomfCoreProviders.MANGADEX -> stringResource(Res.string.komf_provider_mangadex)
            KomfCoreProviders.NAUTILJON -> stringResource(Res.string.komf_provider_nautiljon)
            KomfCoreProviders.YEN_PRESS -> stringResource(Res.string.komf_provider_yenpress)
            KomfCoreProviders.VIZ -> stringResource(Res.string.komf_provider_viz)
            KomfCoreProviders.MANGA_BAKA -> stringResource(Res.string.komf_provider_mangabaka)
            KomfCoreProviders.WEBTOONS -> stringResource(Res.string.komf_provider_webtoons)
            is UnknownKomfProvider -> provider.name
        }

    private val codeMap: Map<String, StringResource> = mapOf(
        "ERR_1000" to Res.string.komga_error_code_1000,
        "ERR_1001" to Res.string.komga_error_code_1001,
        "ERR_1002" to Res.string.komga_error_code_1002,
        "ERR_1003" to Res.string.komga_error_code_1003,
        "ERR_1004" to Res.string.komga_error_code_1004,
        "ERR_1005" to Res.string.komga_error_code_1005,
        "ERR_1006" to Res.string.komga_error_code_1006,
        "ERR_1007" to Res.string.komga_error_code_1007,
        "ERR_1008" to Res.string.komga_error_code_1008,
        "ERR_1009" to Res.string.komga_error_code_1009,
        "ERR_1015" to Res.string.komga_error_code_1015,
        "ERR_1016" to Res.string.komga_error_code_1016,
        "ERR_1017" to Res.string.komga_error_code_1017,
        "ERR_1018" to Res.string.komga_error_code_1018,
        "ERR_1019" to Res.string.komga_error_code_1019,
        "ERR_1020" to Res.string.komga_error_code_1020,
        "ERR_1021" to Res.string.komga_error_code_1021,
        "ERR_1022" to Res.string.komga_error_code_1022,
        "ERR_1023" to Res.string.komga_error_code_1023,
        "ERR_1024" to Res.string.komga_error_code_1024,
        "ERR_1025" to Res.string.komga_error_code_1025,
        "ERR_1026" to Res.string.komga_error_code_1026,
        "ERR_1027" to Res.string.komga_error_code_1027,
        "ERR_1028" to Res.string.komga_error_code_1028,
        "ERR_1029" to Res.string.komga_error_code_1029,
        "ERR_1030" to Res.string.komga_error_code_1030,
        "ERR_1031" to Res.string.komga_error_code_1031,
        "ERR_1032" to Res.string.komga_error_code_1032,
        "ERR_1033" to Res.string.komga_error_code_1033,
        "ERR_1034" to Res.string.komga_error_code_1034,
        "ERR_1035" to Res.string.komga_error_code_1035,
        "ERR_1036" to Res.string.komga_error_code_1036,
        "ERR_1037" to Res.string.komga_error_code_1037,
        "ERR_1038" to Res.string.komga_error_code_1038,
        "ERR_1039" to Res.string.komga_error_code_1039,
    )

    @Composable
    fun getMessageStringForCode(code: String): String {
        return codeMap[code]?.let { stringResource(it) }
            ?: stringResource(Res.string.komga_error_code_unknown)
    }
}

@Composable
fun <T> stringLabels(
    entries: List<T>,
    stringTransform: (T) -> StringResource
): List<LabeledEntry<T>> {
    var labels by remember { mutableStateOf(emptyList<LabeledEntry<T>>()) }
    val environment = rememberResourceEnvironment()
    LaunchedEffect(environment) {
        labels = entries.map { entry ->
            val res = stringTransform(entry)
            val str = getString(environment, res)
            LabeledEntry(entry, str)
        }
    }
    return labels
}
