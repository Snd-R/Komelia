package io.github.snd_r.komelia.updates

import kotlinx.coroutines.flow.Flow
import org.jetbrains.skiko.URIManager

class DesktopAppUpdater(
    private val githubClient: GithubClient
) : AppUpdater {
    override suspend fun getReleases(): List<AppRelease> {
        return githubClient.getReleases().map {
            AppRelease(
                version = AppVersion.fromString(it.tagName),
                publishDate = it.publishedAt,
                releaseNotesBody = it.body.replace("\r", ""),
                htmlUrl = it.htmlUrl,
                assetName = null,
                assetUrl = null
            )
        }
    }

    override suspend fun updateToLatest(): Flow<DownloadProgress>? {
        val latest = githubClient.getLatestRelease()
        URIManager().openUri(latest.htmlUrl)
        return null
    }

    override fun updateTo(release: AppRelease): Flow<DownloadProgress>? {
        URIManager().openUri(release.htmlUrl)
        return null
    }
}