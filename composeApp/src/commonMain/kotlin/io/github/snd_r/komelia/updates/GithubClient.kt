package io.github.snd_r.komelia.updates

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val repoBaseUrl = "https://api.github.com/repos/Snd-R/Komelia"

class GithubClient(private val ktor: HttpClient) {

    suspend fun getReleases(): List<GithubRelease> {
        return ktor.get("$repoBaseUrl/releases") {
            parameter("per_page", 5)
        }.body()
    }

    suspend fun getLatestRelease(): GithubRelease {
        return ktor.get("$repoBaseUrl/releases/latest").body()
    }

    suspend fun streamFile(url: String, block: suspend (response: HttpResponse) -> Unit) {
        ktor.prepareGet(url).execute(block)
    }
}

@Serializable
data class GithubRelease(
    val id: Int,
    @SerialName("published_at")
    val publishedAt: Instant,
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("html_url")
    val htmlUrl: String,
    val body: String,
    val assets: List<GithubReleaseAsset>
)

@Serializable
data class GithubReleaseAsset(
    val id: Int,
    val name: String,
    @SerialName("content_type")
    val contentType: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String
)