ALTER TABLE AppSettings
    DROP COLUMN update_last_checked_timestamp;
ALTER TABLE AppSettings
    ADD COLUMN update_last_checked_timestamp TEXT;
ALTER TABLE AppSettings
    RENAME COLUMN onnxRuntime_device_dd TO onnxRuntime_device_id;
