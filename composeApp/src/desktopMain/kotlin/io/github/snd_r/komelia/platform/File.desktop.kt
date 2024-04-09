package io.github.snd_r.komelia.platform

import com.darkrockstudios.libraries.mpfilepicker.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun PlatformFile.size(): Long = withContext(Dispatchers.IO) { file.length() }

actual suspend fun PlatformFile.getBytes() = withContext(Dispatchers.IO) { file.readBytes() }
