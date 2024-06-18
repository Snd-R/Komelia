package io.github.snd_r.komelia.updates

import kotlinx.coroutines.flow.Flow
import org.jetbrains.skiko.URIManager

class DesktopAppUpdater(
    private val updateClient: UpdateClient
) : AppUpdater {
    override suspend fun getReleases(): List<AppRelease> {
        return updateClient.getKomeliaReleases().map {
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

    override suspend fun updateToLatest(): Flow<UpdateProgress>? {
        val latest = updateClient.getKomeliaLatestRelease()
        URIManager().openUri(latest.htmlUrl)
        return null
    }

    override fun updateTo(release: AppRelease): Flow<UpdateProgress>? {
        URIManager().openUri(release.htmlUrl)
        return null
    }
}