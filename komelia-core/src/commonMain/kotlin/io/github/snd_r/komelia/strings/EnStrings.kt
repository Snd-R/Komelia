package io.github.snd_r.komelia.strings

val EnStrings = AppStrings(
    filters = FilterStrings(
        anyValue = "Any",

        filterTagsSearch = "Search tags",
        filterTagsReset = "Reset",
        filterTagsGenreLabel = "Genre",
        filterTagsTagsLabel = "Tags",
        filterTagsShowMore = "Show more",
        filterTagsShowLess = "Show less",
    ),
    seriesFilter = SeriesFilterStrings(
        resetFilters = "Reset filters",
        hideFilters = "Hide filters",
        anyValue = "Any",
        search = "Search",

        sort = "Sort by",
        sortTitleAsc = "Title Ascending",
        sortTitleDesc = "Title Descending",
        sortDateAddedAsc = "Oldest Added",
        sortDateAddedDesc = "Recently Added",
        sortReleaseDateAsc = "Oldest Release Date",
        sortReleaseDateDesc = "Latest Release Date",
        sortUpdatedAsc = "Oldest Update",
        sortUpdatedDesc = "Latest Update",
        sortFolderNameAsc = "Folder Name Ascending",
        sortFolderNameDesc = "Folder Name Descending",
        sortBooksCountAsc = "Least Books",
        sortBooksCountDesc = "Most Books",

        filterTagsLabel = "Filter tags",
        readStatus = "Read status",
        readStatusUnread = "Unread",
        readStatusInProgress = "In progress",
        readStatusRead = "Read",

        publicationStatus = "Publication status",
        pubStatusEnded = "Ended",
        pubStatusOngoing = "Ongoing",
        pubStatusAbandoned = "Abandoned",
        pubStatusHiatus = "Hiatus",

        complete = "Complete",
        oneshot = "Oneshot",
        authors = "Authors",
        publisher = "Publisher",
        language = "Language",
        releaseDate = "Release date",
        ageRating = "Age rating"
    ),
    booksFilter = BookFilterStrings(
        sort = "Sort order",
        sortNumberAsc = "Ascending",
        sortNumberDesc = "Descending",

        sortFileNameAsc = "Filename ascending",
        sortFileNameDesc = "Filename descending",
        sortReleaseDateAsc = "Latest release date",
        sortReleaseDateDesc = "Oldest release date",

        readStatus = "Read status",
        readStatusUnread = "Unread",
        readStatusInProgress = "In progress",
        readStatusRead = "Read",

        authors = "Authors",
        tags = "Filter Tags",
    ),
    seriesView = SeriesViewStrings(
        statusEnded = "Ended",
        statusOngoing = "Ongoing",
        statusAbandoned = "Abandoned",
        statusHiatus = "Hiatus",
        readingDirectionLeftToRight = "Left to right",
        readingDirectionRightToLeft = "Right to left",
        readingDirectionVertical = "Vertical",
        readingDirectionWebtoon = "Webtoon",
    ),
    seriesEdit = SeriesEditStrings(
        title = "Title",
        sortTitle = "Sort title",
        summary = "Summary",
        language = "Language",

        status = "Status",
        statusEnded = "Ended",
        statusOngoing = "Ongoing",
        statusAbandoned = "Abandoned",
        statusHiatus = "Hiatus",

        readingDirection = "Reading Direction",
        readingDirectionLeftToRight = "Left to right",
        readingDirectionRightToLeft = "Right to left",
        readingDirectionVertical = "Vertical",
        readingDirectionWebtoon = "Webtoon",
        publisher = "Publisher",
        ageRating = "Age rating",
        totalBookCount = "Total book count"
    ),
    bookEdit = BookEditStrings(
        title = "Title",
        number = "Number",
        sortNumber = "Sort Number",
        summary = "Summary",
        releaseDate = "Release date",
        isbn = "ISBN"
    ),
    libraryEdit = LibraryEditStrings(
        emptyTrashAfterScan = "Empty trash automatically after every scan",
        scanForceModifiedTime = "Force directory modified time",
        scanOnStartup = "Scan on startup",
        oneshotsDirectory = "One-Shots directory",
        excludeDirectories = "Directory exclusions",

        scanInterval = "Scan interval",
        scanIntervalDisabled = "Disabled",
        scanIntervalHourly = "Hourly",
        scanIntervalEvery6H = "Every 6 hours",
        scanIntervalEvery12H = "Every 12 hours",
        scanIntervalDaily = "Daily",
        scanIntervalWeekly = "Weekly",

        hashFiles = "Compute hash for files",
        hashPages = "Compute hash for pages",
        analyzeDimensions = "Analyze pages dimensions",
        repairExtensions = "Automatically repair incorrect file extensions",
        convertToCbz = "Automatically convert to CBZ",
        seriesCover = "Series cover",
        coverFirst = "First",
        coverFirstUnreadOrFirst = "First unread else first",
        coverFirstUnreadOrLast = "First unread else last",
        coverLast = "Last"
    ),
    userEdit = UserEditStrings(
        contentRestrictions = "Content restrictions",
        age = "Age",
        labelsAllow = "Allow only labels",
        labelsExclude = "Exclude labels",
        ageRestriction = "Age restrictions",
        ageRestrictionNone = "No restriction",
        ageRestrictionAllowOnly = "Allow only under",
        ageRestrictionExclude = "Exclude over"
    ),
    reader = ReaderStrings(
        zoom = "Zoom",
        readerType = "Reader type",
        readerPaged = "Paged",
        readerContinuous = "Continuous",
        stretchToFit = "Stretch small images",
        decoder = "Image Decoder/Sampler",
        pagesInfo = "Pages info",
        pageNumber = "page",
        memoryUsage = "Memory usage",
        pageDisplaySize = "display size",
        pageOriginalSize = "original size",
    ),
    pagedReader = PagedReaderStrings(
        scaleType = "Scale type",
        scaleScreen = "Screen",
        scaleFitWidth = "Fit width",
        scaleFitHeight = "Fit height",
        scaleOriginal = "Original",
        readingDirection = "Reading direction",
        readingDirectionLeftToRight = "Left to right",
        readingDirectionRightToLeft = "Right to left",
        layout = "Page layout",
        layoutSinglePage = "Single page",
        layoutDoublePages = "Double pages",
        layoutDoublePagesNoCover = "Double pages (no cover)",
        offsetPages = "Offset pages",
    ),
    continuousReader = ContinuousReaderStrings(
        sidePadding = "Side padding",
        pageSpacing = "Page spacing",
        readingDirection = "Reading direction",
        readingDirectionTopToBottom = "Top to bottom",
        readingDirectionLeftToRight = "Left to right",
        readingDirectionRightToLeft = "Right to left",
    ),
    settings = SettingsStrings(
        serverSettings = "Server Settings",
        thumbnailSize = "Thumbnail size",
        thumbnailSizeDefault = "Default (300px)",
        thumbnailSizeMedium = "Medium (600px)",
        thumbnailSizeLarge = "Large (900px)",
        thumbnailSizeXLarge = "X-Large (1200px)",

        thumbnailRegenTitle = "Regenerate thumbnails",
        thumbnailRegenBody = "Thumbnails size has changed. Do you want to regenerate book thumbnails?",
        thumbnailRegenIfBigger = "YES, BUT ONLY IF BIGGER",
        thumbnailRegenAllBooks = "YES, ALL BOOKS",
        thumbnailRegenNo = "NO",

        deleteEmptyCollections = "Delete empty collections after scan",
        deleteEmptyReadLists = "Delete empty read lists after scan",
        taskPoolSize = "Task threads",
        rememberMeDurationDays = "Remember me duration (in days)",
        renewRememberMeKey = "Regenerate RememberMe key",
        serverPort = "Server port",
        serverContextPath = "Base URL",
        requiresRestart = "Requires restart to take effect",
        serverSettingsDiscard = "Discard",
        serverSettingsSave = "Save Changes",

        appTheme = "App Theme",
        appThemeDark = "Dark",
        appThemeLight = "Light",
        imageCardSize = "Image card size in grid view (minimum display size)",
        decoder = "Image Decoder/Sampler",
        epubReaderTypeKomga = "Komga Epub Reader",
        epubReaderTypeTtsu = "ッツ Ebook Reader"
    ),
    errorCodes = ErrorCodes(
        err1000 = "File could not be accessed during analysis",
        err1001 = "Media type is not supported",
        err1002 = "Encrypted RAR archives are not supported",
        err1003 = "Solid RAR archives are not supported",
        err1004 = "Multi-Volume RAR archives are not supported",
        err1005 = "Unknown error while analyzing book",
        err1006 = "Book does not contain any page",
        err1007 = "Some entries could not be analyzed",
        err1008 = "Unknown error while getting book's entries",
        err1009 = "A read list with that name already exists",
        err1015 = "Error while deserializing ComicRack CBL",
        err1016 = "Directory not accessible or not a directory",
        err1017 = "Cannot scan folder that is part of an existing library",
        err1018 = "File not found",
        err1019 = "Cannot import file that is part of an existing library",
        err1020 = "Book to upgrade does not belong to provided series",
        err1021 = "Destination file already exists",
        err1022 = "Newly imported book could not be scanned",
        err1023 = "Book already present in ReadingList",
        err1024 = "OAuth2 login error: no email attribute",
        err1025 = "OAuth2 login error: no local user exist with that email",
        err1026 = "OpenID Connect login error: email not verified",
        err1027 = "OpenID Connect login error: no email_verified attribute",
        err1028 = "OpenID Connect login error: no email attribute",
        err1029 = "ComicRack CBL does not contain any Book element",
        err1030 = "ComicRack CBL has no Name element",
        err1031 = "ComicRack CBL Book is missing series or number",
        err1032 = "EPUB file has wrong media type",
        err1033 = "Some entries are missing",
    ),
    komf = KomfStrings(
        providerSettings = KomfProviderSettingsStrings(
            providerAniList = "AniList",
            providerBangumi = "Bangumi",
            providerBookWalker = "Bookwalker Global",
            providerComicVine = "ComicVine",
            providerHentag = "Hentag",
            providerKodansha = "Kodansha US",
            providerMal = "MyAnimeList",
            providerMangaUpdates = "MangaUpdates",
            providerMangaDex = "MangaDex",
            providerNautiljon = "Nautiljon",
            providerYenPress = "YenPress",
            providerViz = "Viz",
        )
    ),
)