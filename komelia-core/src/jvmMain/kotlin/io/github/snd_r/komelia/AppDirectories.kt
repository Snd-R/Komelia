package io.github.snd_r.komelia

import dev.dirs.ProjectDirectories
import java.nio.file.Path
import kotlin.io.path.Path

object AppDirectories {
    val projectDirectories = ProjectDirectories.from("io.github.snd-r.komelia", "", "Komelia")
    val onnxRuntimeInstallPath: Path = Path(projectDirectories.dataDir).resolve("onnxruntime")
    val mangaJaNaiInstallPath: Path = Path(projectDirectories.dataDir).resolve("mangajanai")
    val mangaJaNaiModelFiles = listOf(
        "2x_MangaJaNai_1200p_V1_ESRGAN_70k.onnx",
        "2x_MangaJaNai_1300p_V1_ESRGAN_75k.onnx",
        "2x_MangaJaNai_1400p_V1_ESRGAN_70k.onnx",
        "2x_MangaJaNai_1500p_V1_ESRGAN_90k.onnx",
        "2x_MangaJaNai_1600p_V1_ESRGAN_90k.onnx",
        "2x_MangaJaNai_1920p_V1_ESRGAN_70k.onnx",
        "2x_MangaJaNai_2048p_V1_ESRGAN_95k.onnx",

//        "4x_MangaJaNai_1200p_V1_ESRGAN_70k.onnx",
//        "4x_MangaJaNai_1300p_V1_ESRGAN_75k.onnx",
//        "4x_MangaJaNai_1400p_V1_ESRGAN_105k.onnx",
//        "4x_MangaJaNai_1500p_V1_ESRGAN_105k.onnx",
//        "4x_MangaJaNai_1600p_V1_ESRGAN_70k.onnx",
//        "4x_MangaJaNai_1920p_V1_ESRGAN_105k.onnx",
//        "4x_MangaJaNai_2048p_V1_ESRGAN_70k.onnx",

        "4x_IllustrationJaNai_V1_ESRGAN_135k.onnx",
    )

    private val cachePath: Path = Path(System.getProperty("java.io.tmpdir")).resolve("komelia")
    val okHttpCachePath: Path = cachePath.resolve("okHttp")
    val coilCachePath: Path = cachePath.resolve("coil")
    val readerCachePath: Path = cachePath.resolve("reader")
    val readerUpscaleCachePath: Path = cachePath.resolve("reader_upscale")
}