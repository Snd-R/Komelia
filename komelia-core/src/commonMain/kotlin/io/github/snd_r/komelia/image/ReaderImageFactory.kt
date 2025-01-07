package io.github.snd_r.komelia.image

import snd.komelia.image.KomeliaImage

interface ReaderImageFactory {
    suspend fun getImage(image: KomeliaImage, pageId: ReaderImage.PageId): ReaderImage
}
