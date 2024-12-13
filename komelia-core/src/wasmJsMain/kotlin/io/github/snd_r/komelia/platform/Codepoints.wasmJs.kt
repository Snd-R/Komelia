package io.github.snd_r.komelia.platform

actual fun codepointsCount(string: String): Long {
    return jsCodepoints(string).toLong()
}

private fun jsCodepoints(s: String): Int {
    js("return Array.from(s).length;")
}
