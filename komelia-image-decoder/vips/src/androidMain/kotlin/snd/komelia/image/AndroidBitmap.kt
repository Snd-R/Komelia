package snd.komelia.image

import android.graphics.Bitmap
import android.hardware.HardwareBuffer
import android.os.Build

object AndroidBitmap {
    fun KomeliaImage.toBitmap(): Bitmap {
        return this.toVipsImage().toBitmap()
    }

    fun VipsImage.toBitmap(): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hardwareBuffer = createHardwareBuffer(this)
            val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, null)
            hardwareBuffer.close()
            checkNotNull(bitmap)
        } else createSoftwareBitmap(this)
    }

    private external fun createHardwareBuffer(image: VipsImage): HardwareBuffer
    private external fun createSoftwareBitmap(image: VipsImage): Bitmap
}