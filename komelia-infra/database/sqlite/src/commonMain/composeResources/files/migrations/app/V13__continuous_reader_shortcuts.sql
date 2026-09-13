ALTER TABLE ImageReaderSettings
    ADD COLUMN continuous_scroll_step REAL DEFAULT 100.0 NOT NULL;
ALTER TABLE ImageReaderSettings
    ADD COLUMN continuous_shortcuts TEXT DEFAULT '{}' NOT NULL;
