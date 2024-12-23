package io.github.snd_r.komelia.curves

import kotlin.jvm.JvmInline


 val identityMap = ByteArray(256)
    .apply { for (i in (0..<256)) this[i] = i.toUByte().toByte() }
@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class LookupTable(val values: UByteArray)