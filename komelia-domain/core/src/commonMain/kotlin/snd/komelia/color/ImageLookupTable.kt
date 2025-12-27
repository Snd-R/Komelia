package snd.komelia.color

val identityMap by lazy {
    UByteArray(256).apply { for (i in (0..<256)) this[i] = i.toUByte() }
}

data class ChannelsLookupTable(
    val value: UByteArray?,
    val rgba: RGBA8888LookupTable?
)

class RGBA8888LookupTable(
    val red: UByteArray,
    val green: UByteArray,
    val blue: UByteArray,
    val alpha: UByteArray,
) {
    constructor(allChannels: UByteArray) : this(allChannels, allChannels, allChannels, identityMap)

    val interleaved by lazy {
        UByteArray(256 * 4).apply {
            for (i in (0..<256)) {
                val index = i * 4
                set(index, red[i])
                set(index + 1, green[i])
                set(index + 2, blue[i])
                set(index + 3, alpha[i])
            }
        }
    }
}