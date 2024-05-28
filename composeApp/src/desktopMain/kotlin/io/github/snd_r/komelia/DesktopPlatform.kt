package io.github.snd_r.komelia

enum class DesktopPlatform {
    Linux,
    Windows,
    MacOS,
    Unknown;

    companion object {
        val Current: DesktopPlatform by lazy {
            val name = System.getProperty("os.name")
            when {
                name?.startsWith("Linux") == true -> Linux
                name?.startsWith("Win") == true -> Windows
                name?.startsWith("Mac OS X") == true -> MacOS
                else -> Unknown
            }
        }
    }
}
