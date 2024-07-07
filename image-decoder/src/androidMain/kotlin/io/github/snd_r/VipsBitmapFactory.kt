package io.github.snd_r

import android.graphics.Bitmap

object VipsBitmapFactory {
    external fun createHardwareBitmap(image: VipsImage): Bitmap
    external fun createSoftwareBitmap(image: VipsImage): Bitmap
}