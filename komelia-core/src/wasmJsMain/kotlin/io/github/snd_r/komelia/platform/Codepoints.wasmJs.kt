package io.github.snd_r.komelia.platform

actual fun codepointsCount(string: String): Long {
    js("return Array.from(string).length;")
}
