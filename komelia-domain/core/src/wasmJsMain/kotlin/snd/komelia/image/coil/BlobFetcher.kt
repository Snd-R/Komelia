package snd.komelia.image.coil

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import io.github.vinceglb.filekit.core.PlatformFile
import okio.Buffer

class BlobFetcher(
    private val file: PlatformFile,
    private val options: Options,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        return SourceFetchResult(
            source = ImageSource(
                source = Buffer().apply { write(file.readBytes()) },
                fileSystem = options.fileSystem
            ),
            mimeType = null,
            dataSource = DataSource.MEMORY
        )
    }

    class Factory : Fetcher.Factory<PlatformFile> {
        override fun create(
            data: PlatformFile,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher {
            return BlobFetcher(data, options)
        }
    }
}