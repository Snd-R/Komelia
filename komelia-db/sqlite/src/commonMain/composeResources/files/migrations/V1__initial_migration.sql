CREATE TABLE AppSettings
(
    version                             INTEGER NOT NULL PRIMARY KEY,
    username                            TEXT    NOT NULL,
    serverUrl                           TEXT    NOT NULL,

    card_width                          INTEGER NOT NULL,
    series_page_load_size               INTEGER NOT NULL,

    book_page_load_size                 INTEGER NOT NULL,
    book_list_layout                    TEXT    NOT NULL,
    app_theme                           TEXT    NOT NULL,

    check_for_updates_on_startup        BOOLEAN NOT NULL,
    update_last_checked_timestamp       BIGINT,
    update_last_checked_release_version TEXT,
    update_dismissed_version            TEXT,

    upscale_option                      TEXT    NOT NULL,
    downscale_option                    TEXT    NOT NULL,
    onnx_models_path                    TEXT    NOT NULL,
    onnxRuntime_device_dd               INTEGER NOT NULL,
    onnxRuntime_tile_size               INTEGER NOT NULL,

    reader_type                         TEXT    NOT NULL,
    stretch_to_fit                      BOOLEAN NOT NULL,
    paged_scale_type                    TEXT    NOT NULL,
    paged_reading_direction             TEXT    NOT NULL,
    paged_page_layout                   TEXT    NOT NULL,
    continuous_reading_direction        TEXT    NOT NULL,
    continuous_padding                  REAL    NOT NULL,
    continuous_page_spacing             INTEGER NOT NULL,

    komf_enabled                        BOOLEAN NOT NULL,
    komf_mode                           TEXT    NOT NULL,
    komf_remote_url                     TEXT    NOT NULL
);


CREATE TABLE KomgaSeries
(
    id                   TEXT    NOT NULL PRIMARY KEY,
    libraryId            TEXT    NOT NULL,
    name                 TEXT    NOT NULL,
    url                  TEXT    NOT NULL,
    booksCount           INTEGER NOT NULL,
    booksReadCount       INTEGER NOT NULL,
    booksUnreadCount     INTEGER NOT NULL,
    booksInProgressCount INTEGER NOT NULL,
    deleted              BOOLEAN NOT NULL,
    oneshot              BOOLEAN NOT NULL
);

CREATE TABLE KomgaSeriesMetadata
(
    seriesId             TEXT    NOT NULL,
    status               TEXT    NOT NULL,
    statusLock           BOOLEAN NOT NULL,
    title                TEXT    NOT NULL,
    titleLock            BOOLEAN NOT NULL,
    titleSort            TEXT    NOT NULL,
    titleSortLock        BOOLEAN NOT NULL,
    alternateTitlesLock  BOOLEAN NOT NULL,
    publisher            TEXT    NOT NULL,
    publisherLock        BOOLEAN NOT NULL,
    summary              TEXT    NOT NULL,
    summaryLock          BOOLEAN NOT NULL,
    readingDirection     TEXT,
    readingDirectionLock BOOLEAN NOT NULL,
    ageRating            INTEGER,
    ageRatingLock        BOOLEAN NOT NULL,
    language             TEXT    NOT NULL,
    languageLock         BOOLEAN NOT NULL,
    genresLock           BOOLEAN NOT NULL,
    tagsLock             BOOLEAN NOT NULL,
    totalBookCount       INTEGER,
    totalBookCountLock   BOOLEAN NOT NULL,
    sharingLabelsLock    BOOLEAN NOT NULL,
    linksLock            BOOLEAN NOT NULL,
    FOREIGN KEY (seriesId) REFERENCES KomgaSeries (id)
);

CREATE TABLE KomgaSeriesMetadataAlternateTitle
(
    label    TEXT NOT NULL,
    title    TEXT NOT NULL,
    seriesId TEXT NOT NULL,
    FOREIGN KEY (seriesId) REFERENCES KomgaSeries (id)
);

CREATE TABLE KomgaSeriesMetadataGenre
(
    genre    TEXT NOT NULL,
    seriesId TEXT NOT NULL,
    FOREIGN KEY (seriesId) REFERENCES KomgaSeries (id)
);

CREATE TABLE KomgaSeriesMetadataLink
(
    label    TEXT NOT NULL,
    url      TEXT NOT NULL,
    seriesId TEXT NOT NULL,
    FOREIGN KEY (seriesId) REFERENCES KomgaSeries (id)
);

CREATE TABLE KomgaSeriesMetadataSharing
(
    label    TEXT NOT NULL,
    seriesId TEXT NOT NULL,
    FOREIGN KEY (seriesId) REFERENCES KomgaSeries (id)
);

CREATE TABLE KomgaSeriesMetadataTag
(
    genre    TEXT NOT NULL,
    seriesId TEXT NOT NULL,
    FOREIGN KEY (seriesId) REFERENCES KomgaSeries (id)
);

CREATE TABLE KomgaBook
(
    id        TEXT PRIMARY KEY,
    seriesId  TEXT    NOT NULL,
    libraryId TEXT    NOT NULL,
    name      TEXT    NOT NULL,
    url       TEXT    NOT NULL,
    fileSize  INTEGER NOT NULL,
    number    INTEGER NOT NULL,
    fileHash  TEXT    NOT NULL,
    deleted   Boolean NOT NULL,
    oneshot   Boolean NOT NULL
);

CREATE TABLE KomgaBookMetadata
(
    bookId          TEXT    NOT NULL,
    number          INTEGER NOT NULL,
    numberLock      Boolean NOT NULL,
    numberSort      INTEGER NOT NULL,
    releaseDate     INTEGER,
    releaseDateLock Boolean NOT NULL,
    summary         TEXT    NOT NULL,
    summaryLock     BOOLEAN NOT NULL,
    title           TEXT    NOT NULL,
    titleLock       BOOLEAN NOT NULL,
    authorsLock     BOOLEAN NOT NULL,
    tagsLock        BOOLEAN NOT NULL,
    isbn            TEXT    NOT NULL,
    isbnLock        BOOLEAN NOT NULL,
    linksLock       BOOLEAN NOT NULL,
    FOREIGN KEY (bookId) REFERENCES KomgaBook (id)
);

CREATE TABLE KomgaBookMetadataAuthor
(
    bookId TEXT NOT NULL,
    name   TEXT NOT NULL,
    role   TEXT NOT NULL,
    FOREIGN KEY (bookId) REFERENCES KomgaBook (id)
);
CREATE TABLE KomgaBookMetadataLink
(
    bookId TEXT NOT NULL,
    label  TEXT NOT NULL,
    url    TEXT NOT NULL,
    FOREIGN KEY (bookId) REFERENCES KomgaBook (id)
);

CREATE TABLE KomgaBookMetadataTag
(
    bookId TEXT NOT NULL,
    tag    TEXT NOT NULL,
    FOREIGN KEY (bookId) REFERENCES KomgaBook (id)
);

CREATE TABLE KomgaBookMetadataAggregation
(
    seriesId      TEXT NOT NULL,
    releaseDate   INTEGER,
    summary       TEXT NOT NULL,
    summaryNumber TEXT NOT NULL,
    FOREIGN KEY (seriesId) REFERENCES KomgaSeries (id)
);

CREATE TABLE KomgaBookMetadataAggregationAuthor
(
    seriesId TEXT NOT NULL,
    name     TEXT NOT NULL,
    role     TEXT NOT NULL,
    FOREIGN KEY (seriesId) REFERENCES KomgaSeries (id)
);

CREATE TABLE KomgaBookMetadataAggregationTag
(
    seriesId TEXT NOT NULL,
    tag      TEXT NOT NULL,
    FOREIGN KEY (seriesId) REFERENCES KomgaSeries (id)
);

CREATE TABLE KomgaCollection
(
    id               TEXT    NOT NULL PRIMARY KEY,
    name             TEXT    NOT NULL,
    ordered          BOOLEAN NOT NULL,
    seriesCount      INTEGER NOT NULL,
    createdDate      INTEGER NOT NULL,
    lastModifiedDate INTEGER NOT NULL
);

CREATE TABLE KomgaCollectionSeries
(
    collectionId TEXT    NOT NULL,
    seriesId     TEXT    NOT NULL,
    number       INTEGER NOT NULL,
    FOREIGN KEY (collectionId) REFERENCES KomgaCollection (id),
    FOREIGN KEY (seriesId) REFERENCES KomgaSeries (id)
);
