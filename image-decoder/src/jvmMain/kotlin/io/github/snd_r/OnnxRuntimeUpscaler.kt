package io.github.snd_r

object OnnxRuntimeUpscaler {
    external fun upscale(image: VipsImage, modelPath: String): VipsImage

    external fun closeCurrentSession()

    external fun init(provider: String)
}