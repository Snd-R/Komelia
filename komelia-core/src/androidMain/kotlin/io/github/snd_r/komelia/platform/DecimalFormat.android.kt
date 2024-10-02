package io.github.snd_r.komelia.platform

actual fun Float.formatDecimal(numberOfDecimals: Int) = "%.${numberOfDecimals}f".format(this)
