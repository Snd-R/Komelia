package snd.komelia.offline.mediacontainer

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

class SafSeekableReadByteChannel(
    treeUri: Uri,
    context: Context,
) : SeekableByteChannel {
    private val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(treeUri, "r")
        ?: error("Failed to open file descriptor $treeUri")
    private val fileStream: FileInputStream = FileInputStream(pfd.fileDescriptor)
    private val fileChannel = fileStream.channel

    private var position: Long = 0

    override fun position(): Long {
        return position
    }

    override fun position(newPosition: Long): SeekableByteChannel {
        position = newPosition;
        return this;
    }

    override fun read(dst: ByteBuffer?): Int {
        try {
            fileChannel.position(position);
            val bytesRead = fileChannel.read(dst);
            position = fileChannel.position();
            return bytesRead;
        } catch (e: IOException) {
            e.printStackTrace();
            return -1;
        }
    }

    override fun size(): Long {
        return fileChannel.size();
    }

    override fun truncate(size: Long): SeekableByteChannel {
        fileChannel.truncate(size);
        return this;
    }

    override fun write(src: ByteBuffer?): Int {
        TODO("Not yet implemented")
    }

    override fun close() {
        fileStream.close()
        pfd.close()
    }

    override fun isOpen(): Boolean {
        return fileChannel.isOpen
    }
}