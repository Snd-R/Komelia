ALTER TABLE ImageReaderSettings
    ADD COLUMN flash_on_page_change BOOLEAN DEFAULT 0 NOT NULL;
ALTER TABLE ImageReaderSettings
    ADD COLUMN flash_duration INTEGER DEFAULT 100 NOT NULL;
ALTER TABLE ImageReaderSettings
    ADD COLUMN flash_every_n_pages INTEGER DEFAULT 1 NOT NULL;
ALTER TABLE ImageReaderSettings
    ADD COLUMN flash_with TEXT DEFAULT 'BLACK' NOT NULL;
