CREATE TABLE OFFLINE_MEDIA_SERVER
(
    id  TEXT NOT NULL PRIMARY KEY,
    url TEXT NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS offline_media_server__url_idx ON OFFLINE_MEDIA_SERVER (url);


CREATE TABLE LIBRARY
(
    id                                     TEXT    NOT NULL PRIMARY KEY,
    server_id                              TEXT    NOT NULL,
    name                                   TEXT    NOT NULL,
    root                                   TEXT    NOT NULL,
    import_comic_info_book                 BOOLEAN NOT NULL,
    import_comic_info_series               BOOLEAN NOT NULL,
    import_comic_info_collection           BOOLEAN NOT NULL,
    import_comic_info_read_list            BOOLEAN NOT NULL,
    import_comic_info_series_append_volume BOOLEAN NOT NULL,
    import_epub_book                       BOOLEAN NOT NULL,
    import_epub_series                     BOOLEAN NOT NULL,
    import_mylar_series                    BOOLEAN NOT NULL,
    import_local_artwork                   BOOLEAN NOT NULL,
    import_barcode_isbn                    BOOLEAN NOT NULL,
    scan_force_modified_time               BOOLEAN NOT NULL,
    scan_on_startup                        BOOLEAN NOT NULL,
    scan_interval                          TEXT    NOT NULL,
    scan_cbx                               BOOLEAN NOT NULL,
    scan_pdf                               BOOLEAN NOT NULL,
    scan_epub                              BOOLEAN NOT NULL,
    repair_extensions                      BOOLEAN NOT NULL,
    convert_to_cbz                         BOOLEAN NOT NULL,
    empty_trash_after_scan                 BOOLEAN NOT NULL,
    series_cover                           TEXT    NOT NULL,
    hash_files                             BOOLEAN NOT NULL,
    hash_pages                             BOOLEAN NOT NULL,
    hash_koreader                          BOOLEAN NOT NULL,
    analyze_dimensions                     BOOLEAN NOT NULL,
    oneshots_directory                     TEXT,
    unavailable                            BOOLEAN NOT NULL,
    FOREIGN KEY (server_id) REFERENCES OFFLINE_MEDIA_SERVER (id)
);

CREATE TABLE USER
(
    id                         TEXT    NOT NULL PRIMARY KEY,
    server_id                  TEXT,
    email                      TEXT    NOT NULL,
    shared_all_libraries       BOOLEAN NOT NULL,
    age_restriction            INTEGER,
    age_restriction_allow_only BOOLEAN,
    FOREIGN KEY (server_id) REFERENCES OFFLINE_MEDIA_SERVER (id)
);

CREATE TABLE USER_LIBRARY_SHARING
(
    user_id    TEXT NOT NULL,
    library_id TEXT NOT NULL,
    PRIMARY KEY (user_id, library_id),
    FOREIGN KEY (user_id) REFERENCES USER (id),
    FOREIGN KEY (library_id) REFERENCES LIBRARY (id)
);

CREATE TABLE USER_ROLE
(
    user_id TEXT NOT NULL,
    role    TEXT NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES USER (id)
);

CREATE TABLE USER_SHARING
(
    user_id TEXT    NOT NULL,
    label   TEXT    NOT NULL,
    allow   BOOLEAN NOT NULL,
    PRIMARY KEY (user_id, label, allow),
    FOREIGN KEY (user_id) REFERENCES USER (id)
);

CREATE TABLE LIBRARY_EXCLUSIONS
(
    library_id TEXT NOT NULL,
    exclusion  TEXT NOT NULL,
    PRIMARY KEY (library_id, exclusion),
    FOREIGN KEY (library_id) REFERENCES LIBRARY (id)
);

CREATE TABLE SERIES
(
    id                      TEXT     NOT NULL PRIMARY KEY,
    library_id              TEXT     NOT NULL,
    name                    TEXT     NOT NULL,
    url                     TEXT     NOT NULL,
    books_count             INTEGER  NOT NULL,
    deleted                 BOOLEAN  NOT NULL,
    oneshot                 BOOLEAN  NOT NULL,
    created_date            DATETIME NOT NULL DEFAULT (unixepoch()),
    last_modified_date      DATETIME NOT NULL DEFAULT (unixepoch()),
    file_last_modified_date DATETIME NOT NULL DEFAULT (unixepoch())
);

CREATE TABLE SERIES_METADATA
(
    series_id              TEXT    NOT NULL PRIMARY KEY,
    status                 TEXT    NOT NULL,
    status_lock            BOOLEAN NOT NULL,
    title                  TEXT    NOT NULL,
    title_lock             BOOLEAN NOT NULL,
    title_sort             TEXT    NOT NULL,
    title_sort_lock        BOOLEAN NOT NULL,
    alternate_titles_lock  BOOLEAN NOT NULL,
    publisher              TEXT    NOT NULL,
    publisher_lock         BOOLEAN NOT NULL,
    summary                TEXT    NOT NULL,
    summary_lock           BOOLEAN NOT NULL,
    reading_direction      TEXT,
    reading_direction_lock BOOLEAN NOT NULL,
    age_rating             INTEGER,
    age_rating_lock        BOOLEAN NOT NULL,
    language               TEXT    NOT NULL,
    language_lock          BOOLEAN NOT NULL,
    genres_lock            BOOLEAN NOT NULL,
    tags_lock              BOOLEAN NOT NULL,
    total_book_count       INTEGER,
    total_book_count_lock  BOOLEAN NOT NULL,
    sharing_labels_lock    BOOLEAN NOT NULL,
    links_lock             BOOLEAN NOT NULL,
    FOREIGN KEY (series_id) REFERENCES SERIES (id)
);

CREATE TABLE SERIES_METADATA_ALTERNATE_TITLE
(
    series_id TEXT NOT NULL,
    label     TEXT NOT NULL,
    title     TEXT NOT NULL,
    PRIMARY KEY (series_id, label, title),
    FOREIGN KEY (series_id) REFERENCES SERIES (id)
);

CREATE TABLE SERIES_METADATA_GENRE
(
    series_id TEXT NOT NULL,
    genre     TEXT NOT NULL,
    PRIMARY KEY (series_id, genre),
    FOREIGN KEY (series_id) REFERENCES SERIES (id)
);

CREATE TABLE SERIES_METADATA_LINK
(
    series_id TEXT NOT NULL,
    label     TEXT NOT NULL,
    url       TEXT NOT NULL,
    PRIMARY KEY (series_id, label, url),
    FOREIGN KEY (series_id) REFERENCES SERIES (id)
);

CREATE TABLE SERIES_METADATA_SHARING
(
    series_id TEXT NOT NULL,
    label     TEXT NOT NULL,
    PRIMARY KEY (series_id, label),
    FOREIGN KEY (series_id) REFERENCES SERIES (id)
);

CREATE TABLE SERIES_METADATA_TAG
(
    series_id TEXT NOT NULL,
    tag       TEXT NOT NULL,
    PRIMARY KEY (series_id, tag),
    FOREIGN KEY (series_id) REFERENCES SERIES (id)
);

CREATE TABLE BOOK
(
    id                        TEXT PRIMARY KEY,
    series_id                 TEXT     NOT NULL,
    library_id                TEXT     NOT NULL,
    name                      TEXT     NOT NULL,
    url                       TEXT     NOT NULL,
    file_size                 INTEGER  NOT NULL,
    number                    INTEGER  NOT NULL,
    file_hash                 TEXT     NOT NULL,
    deleted                   BOOLEAN  NOT NULL,
    oneshot                   BOOLEAN  NOT NULL,

    created_date              DATETIME NOT NULL DEFAULT (unixepoch()),
    last_modified_date        DATETIME NOT NULL DEFAULT (unixepoch()),

    remote_file_modified_date DATETIME NOT NULL DEFAULT (unixepoch()),
    local_file_modified_date  DATETIME NOT NULL DEFAULT (unixepoch()),
    remote_unavailable        BOOLEAN  NOT NULL,
    file_download_path        TEXT     NOT NULL,

    FOREIGN KEY (series_id) REFERENCES SERIES (id),
    FOREIGN KEY (library_id) REFERENCES LIBRARY (id)
);

CREATE TABLE BOOK_METADATA
(
    book_id            TEXT     NOT NULL PRIMARY KEY,
    number             TEXT     NOT NULL,
    number_lock        BOOLEAN  NOT NULL,
    number_sort        REAL     NOT NULL,
    number_sort_lock   BOOLEAN  NOT NULL,
    release_date       DATETIME,
    release_date_lock  BOOLEAN  NOT NULL,
    summary            TEXT     NOT NULL,
    summary_lock       BOOLEAN  NOT NULL,
    title              TEXT     NOT NULL,
    title_lock         BOOLEAN  NOT NULL,
    authors_lock       BOOLEAN  NOT NULL,
    tags_lock          BOOLEAN  NOT NULL,
    isbn               TEXT     NOT NULL,
    isbn_lock          BOOLEAN  NOT NULL,
    links_lock         BOOLEAN  NOT NULL,
    created_date       DATETIME NOT NULL DEFAULT (unixepoch()),
    last_modified_date DATETIME NOT NULL DEFAULT (unixepoch()),
    FOREIGN KEY (book_id) REFERENCES BOOK (id)
);

CREATE TABLE BOOK_METADATA_AUTHOR
(
    book_id TEXT NOT NULL,
    name    TEXT NOT NULL,
    role    TEXT NOT NULL,
    PRIMARY KEY (book_id, name, role),
    FOREIGN KEY (book_id) REFERENCES BOOK (id)
);
CREATE TABLE BOOK_METADATA_LINK
(
    book_id TEXT NOT NULL,
    label   TEXT NOT NULL,
    url     TEXT NOT NULL,
    PRIMARY KEY (book_id, label, url),
    FOREIGN KEY (book_id) REFERENCES BOOK (id)
);

CREATE TABLE BOOK_METADATA_TAG
(
    book_id TEXT NOT NULL,
    tag     TEXT NOT NULL,
    PRIMARY KEY (book_id, tag),
    FOREIGN KEY (book_id) REFERENCES BOOK (id)
);

CREATE TABLE BOOK_METADATA_AGGREGATION
(
    series_id          TEXT     NOT NULL PRIMARY KEY,
    release_date       INTEGER,
    summary            TEXT     NOT NULL,
    summary_number     TEXT     NOT NULL,
    created_date       DATETIME NOT NULL DEFAULT (unixepoch()),
    last_modified_date DATETIME NOT NULL DEFAULT (unixepoch()),
    FOREIGN KEY (series_id) REFERENCES SERIES (id)
);

CREATE TABLE BOOK_METADATA_AGGREGATION_AUTHOR
(
    series_id TEXT NOT NULL,
    name      TEXT NOT NULL,
    role      TEXT NOT NULL,
    PRIMARY KEY (series_id, name, role),
    FOREIGN KEY (series_id) REFERENCES SERIES (id)
);

CREATE TABLE BOOK_METADATA_AGGREGATION_TAG
(
    series_id TEXT NOT NULL,
    tag       TEXT NOT NULL,
    PRIMARY KEY (series_id, tag),
    FOREIGN KEY (series_id) REFERENCES SERIES (id)
);

CREATE TABLE COLLECTION
(
    id                 TEXT     NOT NULL PRIMARY KEY,
    name               TEXT     NOT NULL,
    ordered            BOOLEAN  NOT NULL,
    series_count       INTEGER  NOT NULL,
    created_date       DATETIME NOT NULL DEFAULT (unixepoch()),
    last_modified_date DATETIME NOT NULL DEFAULT (unixepoch())
);

CREATE TABLE COLLECTION_SERIES
(
    collection_id TEXT    NOT NULL,
    series_id     TEXT    NOT NULL,
    number        INTEGER NOT NULL,
    FOREIGN KEY (collection_id) REFERENCES COLLECTION (id),
    FOREIGN KEY (series_id) REFERENCES SERIES (id)
);

CREATE TABLE READ_PROGRESS
(
    book_id            TEXT     NOT NULL,
    user_id            TEXT     NOT NULL,
    page               INTEGER  NOT NULL,
    completed          BOOLEAN  NOT NULL,
    read_date          DATETIME,
    device_id          TEXT     NOT NULL,
    device_name        TEXT     NOT NULL,
    locator            TEXT,

    created_date       DATETIME NOT NULL DEFAULT (unixepoch()),
    last_modified_date DATETIME NOT NULL DEFAULT (unixepoch()),

    PRIMARY KEY (book_id, user_id),
    FOREIGN KEY (book_id) REFERENCES BOOK (id),
    FOREIGN KEY (user_id) REFERENCES USER (id)
);

CREATE TABLE READ_PROGRESS_SERIES
(
    series_id             TEXT    NOT NULL,
    user_id               TEXT    NOT NULL,
    read_count            INTEGER NOT NULL,
    in_progress_count     INTEGER NOT NULL,
    most_recent_read_date INTEGER,

    PRIMARY KEY (series_id, user_id),
    FOREIGN KEY (series_id) REFERENCES SERIES (id),
    FOREIGN KEY (user_id) REFERENCES USER (id)
);

CREATE TABLE THUMBNAIL_BOOK
(
    id         TEXT    NOT NULL PRIMARY KEY,
    book_id    TEXT    NOT NULL,
    thumbnail  BLOB,
    url        TEXT,
    type       TEXT    NOT NULL,
    selected   BOOLEAN NOT NULL,
    media_type TEXT    NOT NULL,
    file_size  INTEGER NOT NULL,
    width      INTEGER NOT NULL,
    height     INTEGER NOT NULL,
    FOREIGN KEY (book_id) REFERENCES BOOK (id)
);

CREATE TABLE THUMBNAIL_SERIES
(
    id         TEXT    NOT NULL PRIMARY KEY,
    series_id  TEXT    NOT NULL,
    thumbnail  BLOB,
    url        TEXT,
    type       TEXT    NOT NULL,
    selected   BOOLEAN NOT NULL,
    media_type TEXT    NOT NULL,
    file_size  INTEGER NOT NULL,
    width      INTEGER NOT NULL,
    height     INTEGER NOT NULL,
    FOREIGN KEY (series_id) REFERENCES series (id)
);

CREATE TABLE MEDIA
(
    book_id                TEXT    NOT NULL PRIMARY KEY,
    status                 TEXT    NOT NULL,
    media_type             TEXT,
    media_profile          TEXT,
    page_count             INTEGER NOT NULL,
    comment                TEXT,
    epub_divina_compatible BOOLEAN NOT NULL,
    epub_is_kepub          BOOLEAN NOT NULL,
    extension              TEXT,
    FOREIGN KEY (book_id) REFERENCES book (id)
);

CREATE TABLE MEDIA_PAGE
(
    book_id    TEXT    NOT NULL,
    number     INTEGER NOT NULL,
    file_name  TEXT    NOT NULL,
    media_type TEXT    NOT NULL,
    width      INTEGER,
    height     INTEGER,
    file_size  INTEGER,
    PRIMARY KEY (book_id, number)
);


INSERT INTO USER
VALUES ('0', null, 'root', 1, NULL, 0);

CREATE TABLE TASK
(
    unique_name  TEXT     NOT NULL PRIMARY KEY,
    priority     INT      NOT NULL,
    status       TEXT     NOT NULL,
    task         TEXT     NOT NULL,
    created_date DATETIME NOT NULL DEFAULT (unixepoch())
);
CREATE INDEX tasks__status_idx ON TASK (status);
CREATE INDEX tasks__start_time_idx ON TASK (created_date);

CREATE TABLE SETTINGS
(
    version                 INTEGER NOT NULL PRIMARY KEY,
    is_offline_mode_enabled BOOLEAN NOT NULL,
    user_id                 TEXT,
    server_id               TEXT,
    download_directory      TEXT    NOT NULL,
    read_progress_sync_date DATETIME,
    data_sync_date          DATETIME,

    FOREIGN KEY (user_id) REFERENCES USER (id),
    FOREIGN KEY (server_id) REFERENCES OFFLINE_MEDIA_SERVER (id)
);

CREATE TABLE LOG_JOURNAL
(
    id        TEXT     NOT NULL PRIMARY KEY,
    message   TEXT     NOT NULL,
    type      TEXT     NOT NULl,
    timestamp DATETIME NOT NULL
);
CREATE INDEX log_journal__level_idx ON LOG_JOURNAL (type);
