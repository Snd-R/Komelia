package io.github.snd_r.komelia.platform

val skiaSamplerMitchell = UpscaleOption("Bicubic Mitchell-Netravali")
val skiaSamplerCatmullRom = UpscaleOption("Bicubic Catmull-Rom")
val skiaSamplerNearest = UpscaleOption("Nearest neighbour")
val mangaJaNai = UpscaleOption("MangaJaNai")
val vipsDownscaleLanczos = DownscaleOption("Lanczos")

val upsamplingFilters = listOf(skiaSamplerMitchell, skiaSamplerCatmullRom, skiaSamplerNearest)
