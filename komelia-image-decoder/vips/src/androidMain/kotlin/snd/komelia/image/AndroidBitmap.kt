package snd.komelia.image

import android.graphics.Bitmap
import android.os.Build

object AndroidBitmap {
    fun KomeliaImage.toBitmap(): Bitmap {
        return this.toVipsImage().toBitmap()
    }

    fun VipsImage.toBitmap(): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        // Uses Bitmap.wrapHardwareBuffer() added in API 29
            createHardwareBitmap(this)
        else
            createSoftwareBitmap(this)
    }

    private external fun createHardwareBitmap(image: VipsImage): Bitmap
    private external fun createSoftwareBitmap(image: VipsImage): Bitmap
}