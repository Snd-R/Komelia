package io.github.snd_r.komelia.platform

import androidx.core.net.toFile
import com.darkrockstudios.libraries.mpfilepicker.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun PlatformFile.size() = withContext(Dispatchers.IO) { uri.toFile().length() }
actual suspend fun PlatformFile.getBytes() = withContext(Dispatchers.IO) { uri.toFile().readBytes() }