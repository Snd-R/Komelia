package io.github.snd_r.komelia.platform

actual enum class PlatformDecoderType(private val displayName: String) {
    DEFAULT("Default");

    actual fun getDisplayName() = this.displayName

}