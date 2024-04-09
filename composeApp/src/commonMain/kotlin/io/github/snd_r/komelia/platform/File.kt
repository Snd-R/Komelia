package io.github.snd_r.komelia.platform

import com.darkrockstudios.libraries.mpfilepicker.PlatformFile

expect suspend fun PlatformFile.size(): Long
expect suspend fun PlatformFile.getBytes(): ByteArray
