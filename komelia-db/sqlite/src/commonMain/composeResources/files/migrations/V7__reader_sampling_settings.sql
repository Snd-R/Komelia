ALTER TABLE ImageReaderSettings
    ADD COLUMN downsampling_kernel TEXT DEFAULT 'LANCZOS3' NOT NULL;

ALTER TABLE ImageReaderSettings
    ADD COLUMN linear_light_downsampling BOOLEAN DEFAULT 0 NOT NULL;

ALTER TABLE ImageReaderSettings
    ADD COLUMN upsampling_mode TEXT DEFAULT 'CATMULL_ROM' NOT NULL;

ALTER TABLE ImageReaderSettings
    ADD COLUMN onnx_runtime_mode TEXT DEFAULT 'NONE' NULL;

ALTER TABLE ImageReaderSettings
    ADD COLUMN onnx_runtime_device_id INTEGER DEFAULT 0 NOT NULL;

ALTER TABLE ImageReaderSettings
    ADD COLUMN onnx_runtime_tile_size INTEGER DEFAULT 0 NOT NULL;

ALTER TABLE ImageReaderSettings
    ADD COLUMN onnx_runtime_model_path TEXT;

ALTER TABLE AppSettings
    DROP COLUMN upscale_option;

ALTER TABLE AppSettings
    DROP COLUMN downscale_option;

ALTER TABLE AppSettings
    DROP COLUMN onnx_models_path;

ALTER TABLE AppSettings
    DROP COLUMN onnxRuntime_device_id;

ALTER TABLE AppSettings
    DROP COLUMN onnxRuntime_tile_size;

ALTER TABLE AppSettings
    DROP COLUMN image_reader_debug_tile_grid;
