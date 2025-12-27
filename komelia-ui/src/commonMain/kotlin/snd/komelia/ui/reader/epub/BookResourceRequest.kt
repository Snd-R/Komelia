package snd.komelia.ui.reader.epub

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import snd.komelia.komga.api.KomgaBookApi
import snd.komga.client.book.KomgaBookId
import snd.webview.ResourceLoadResult


private val bookResourceRegex = ".*/api/v1/books/(?<bookId>.*)/resource/(?<resourceName>.*)".toRegex()
private val bookManifestRegex = ".*/api/v1/books/(?<bookId>.*)/manifest".toRegex()
private val bookPositionsRegex = ".*/api/v1/books/(?<bookId>.*)/positions".toRegex()

suspend fun proxyResourceRequest(
    bookApi: KomgaBookApi,
    urlString: String,
    serverUrl: Flow<String>
): ResourceLoadResult {
    check(urlString.startsWith(serverUrl.first())) { "Requests to external hosts are not allowed $urlString" }
    val resourceMatch = bookResourceRegex.find(urlString)?.groups
    if (resourceMatch != null) {
        val bookId = resourceMatch["bookId"]?.value ?: error("Failed to find bookId $urlString")
        val resourceName = resourceMatch["resourceName"]?.value ?: error("Failed to find resource name $urlString")
        return ResourceLoadResult(
            data = bookApi.getBookEpubResource(KomgaBookId(bookId), resourceName),
            contentType = null
        )
    }
    val manifestMatch = bookManifestRegex.find(urlString)?.groups
    if (manifestMatch != null) {
        val bookId = manifestMatch["bookId"]?.value ?: error("Failed to find bookId $urlString")
        return ResourceLoadResult(
            data = Json.encodeToString(
                bookApi.getWebPubManifest(KomgaBookId(bookId))
            ).encodeToByteArray(),
            contentType = null
        )
    }
    val bookPositionsMatch = bookPositionsRegex.find(urlString)?.groups
    if (bookPositionsMatch != null) {
        val bookId = bookPositionsMatch["bookId"]?.value ?: error("Failed to find bookId $urlString")

        return ResourceLoadResult(
            data = Json.encodeToString(
                bookApi.getReadiumPositions(KomgaBookId(bookId))
            ).encodeToByteArray(),
            contentType = null
        )
    }

    error("Unsupported resource request $urlString")
}
