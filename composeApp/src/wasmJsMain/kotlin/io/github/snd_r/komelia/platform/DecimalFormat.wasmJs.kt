package io.github.snd_r.komelia.platform

actual fun Float.formatDecimal(numberOfDecimals: Int): String {
    return toFixed(this, numberOfDecimals)
}

private fun toFixed(x: Float, num: Int): String = js("x.toFixed(num)")
