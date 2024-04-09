package io.github.snd_r.komelia.platform

import com.darkrockstudios.libraries.mpfilepicker.PlatformFile
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.files.FileReader
import org.w3c.files.FileReader.Companion.DONE
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual suspend fun PlatformFile.size() = file.size.toInt().toLong()

actual suspend fun PlatformFile.getBytes(): ByteArray {
    return suspendCoroutine { continuation ->
        val reader = FileReader()

        reader.onloadend = {
            if (reader.readyState == DONE) {
                val bytes = Int8Array(reader.result!!.unsafeCast<ArrayBuffer>()).toByteArray()
                continuation.resume(bytes)
            } else {
                continuation.resumeWithException(IllegalStateException("can't load file ${file.name}"))
            }
        }

        reader.readAsArrayBuffer(file)
    }
}
private fun Int8Array.toByteArray(): ByteArray = ByteArray(this.length) { this[it] }
