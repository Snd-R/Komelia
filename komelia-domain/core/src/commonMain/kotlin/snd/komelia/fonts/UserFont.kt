package snd.komelia.fonts

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.random.Random

private val illegalFontNameCharsRegex = "[^A-Za-z0-9 ]".toRegex()

class UserFont(
    val name: String,
    val path: Path,
) {
    val canonicalName get() = name.replace(illegalFontNameCharsRegex, "").ifBlank { "random_${Random.nextInt()}" }

    suspend fun getBytes(): ByteArray {
        return withContext(Dispatchers.Default) {
            SystemFileSystem.source(path).buffered().readByteArray()
        }
    }

//    suspend fun asComposeFont(): Font {
//        return Font(
//            identity = "Komelia_$name",
//            data = getBytes(),
//            weight = FontWeight.Normal,
//            style = FontStyle.Normal
//        )
//    }

    fun deleteFontFile() {
        if (SystemFileSystem.exists(path)) {
            SystemFileSystem.delete(path)
        }
    }

    companion object {
        suspend fun saveFontToAppDirectory(
            name: String,
            file: PlatformFile,
        ): UserFont? {
            val fontsDir = userFontsDirectory() ?: return null
            val fileSystem = SystemFileSystem
            fileSystem.createDirectories(fontsDir)
            val extension = file.name.substringAfterLast(".", "")

            val newFileName = buildString {
                append(name)
                if (extension.isNotBlank() && !name.endsWith(extension)) append(".$extension")
            }
            val newFile = Path(fontsDir, newFileName)
            val bytes = file.readBytes()
            val fileSink = fileSystem.sink(newFile).buffered()
            fileSink.writeFully(bytes)
            fileSink.flush()

            return UserFont(name, newFile)
        }
    }
}

expect fun getSystemFontNames(): List<String>

expect fun userFontsDirectory(): Path?