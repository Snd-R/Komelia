package io.github.snd_r.komelia.fonts

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
        return withContext(Dispatchers.IO) {
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
        fun saveFontToAppDirectory(
            name: String,
            file: Path,
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
            fileSystem.source(file).buffered().transferTo(fileSystem.sink(newFile))

            return UserFont(name, newFile)
        }
    }
}

expect fun getSystemFontNames(): List<String>

expect fun userFontsDirectory(): Path?