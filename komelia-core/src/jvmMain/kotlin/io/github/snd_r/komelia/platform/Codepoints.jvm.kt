package io.github.snd_r.komelia.platform

actual fun codepointsCount(string: String): Long {
    return string.codePoints().count()
}