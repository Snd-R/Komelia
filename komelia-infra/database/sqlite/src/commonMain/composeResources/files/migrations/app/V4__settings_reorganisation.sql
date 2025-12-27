CREATE TABLE EpubReaderSettings
(
    book_id             TEXT PRIMARY KEY,
    reader_type         TEXT NOT NULL,
    komga_settings_json TEXT NOT NULL,
    ttsu_settings_json  TEXT NOT NULL
);

CREATE TABLE ImageReaderSettings
(
    book_id                      TEXT PRIMARY KEY,
    reader_type                  TEXT    NOT NULL,
    stretch_to_fit               BOOLEAN NOT NULL,
    paged_scale_type             TEXT    NOT NULL,
    paged_reading_direction      TEXT    NOT NULL,
    paged_page_layout            TEXT    NOT NULL,
    continuous_reading_direction TEXT    NOT NULL,
    continuous_padding           REAL    NOT NULL,
    continuous_page_spacing      INTEGER NOT NULL,
    crop_borders                 BOOLEAN NOT NULL
);

INSERT INTO ImageReaderSettings (book_id,
                                 reader_type,
                                 stretch_to_fit,
                                 paged_scale_type,
                                 paged_reading_direction,
                                 paged_page_layout,
                                 continuous_reading_direction,
                                 continuous_padding,
                                 continuous_page_spacing,
                                 crop_borders)
SELECT 'DEFAULT',
       reader_type,
       stretch_to_fit,
       paged_scale_type,
       paged_reading_direction,
       paged_page_layout,
       continuous_reading_direction,
       continuous_padding,
       continuous_page_spacing,
       crop_borders
FROM AppSettings;

CREATE TABLE UserFonts
(
    name TEXT PRIMARY KEY,
    path TEXT NOT NULL
);

ALTER TABLE AppSettings
    DROP COLUMN reader_type;
ALTER TABLE AppSettings
    DROP COLUMN stretch_to_fit;
ALTER TABLE AppSettings
    DROP COLUMN paged_scale_type;
ALTER TABLE AppSettings
    DROP COLUMN paged_reading_direction;
ALTER TABLE AppSettings
    DROP COLUMN paged_page_layout;
ALTER TABLE AppSettings
    DROP COLUMN continuous_reading_direction;
ALTER TABLE AppSettings
    DROP COLUMN continuous_padding;
ALTER TABLE AppSettings
    DROP COLUMN continuous_page_spacing;
ALTER TABLE AppSettings
    DROP COLUMN crop_borders;
ALTER TABLE AppSettings
    DROP COLUMN komga_webui_epub_reader_settings;
ALTER TABLE AppSettings
    ADD COLUMN image_reader_debug_tile_grid BOOLEAN NOT NULL default 0;
