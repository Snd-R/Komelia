CREATE TABLE KomfSettings
(
    version    INTEGER NOT NULL PRIMARY KEY,
    enabled    BOOLEAN NOT NULL,
    remote_url TEXT    NOT NULL
);

INSERT INTO KomfSettings
SELECT version, komf_enabled, komf_remote_url
FROM AppSettings;

ALTER TABLE AppSettings
    DROP COLUMN komf_enabled;
ALTER TABLE AppSettings
    DROP COLUMN komf_remote_url;
ALTER TABLE AppSettings
    DROP COLUMN komf_mode;
