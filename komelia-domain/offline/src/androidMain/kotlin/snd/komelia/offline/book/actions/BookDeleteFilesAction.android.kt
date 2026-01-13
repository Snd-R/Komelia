package snd.komelia.offline.book.actions

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import snd.komelia.offline.action.OfflineAction
import java.io.File

actual class BookDeleteFilesAction : OfflineAction {
    actual suspend fun execute(file: PlatformFile) {
        when (val androidFile = file.androidFile) {
            is AndroidFile.FileWrapper -> deleteFile(androidFile.file)
            is AndroidFile.UriWrapper -> deleteSafFile(androidFile.uri)
        }
    }

    private fun deleteFile(file: File) {
        val filePath = Path(file.path)
        val parent = filePath.parent

        SystemFileSystem.delete(filePath, false)

        if (parent != null && SystemFileSystem.exists(parent) && SystemFileSystem.list(parent).isEmpty()) {
            SystemFileSystem.delete(parent, false)
        }
    }

    private fun deleteSafFile(uri: Uri) {
        val context = FileKit.context
        val file = DocumentFile.fromTreeUri(context, uri) ?: error("Can't get file $uri")
        val parent = file.parentFile

        file.delete()
        if (parent != null && parent.exists()) {
            parent.delete()
        }
    }
}