package io.github.snd_r.komelia.image

interface ReaderImageFactory {
    suspend fun getImage(imageSource: ImageSource, pageId: ReaderImage.PageId): ReaderImage
}
