CREATE TABLE KomgaEpubReaderSettings
(
    book_id       TEXT PRIMARY KEY,
    settings_json TEXT NOT NULL
);
CREATE TABLE TtsuEpubReaderSettings
(
    book_id       TEXT PRIMARY KEY,
    settings_json TEXT NOT NULL
);
ALTER TABLE AppSettings
    DROP COLUMN komga_webui_epub_reader_settings;
