package snd.komelia.updates

import androidx.compose.ui.platform.UriHandler
import kotlinx.coroutines.flow.Flow
import snd.komelia.DesktopPlatform
import java.awt.Desktop
import java.net.URI

class DesktopAppUpdater(
    private val updateClient: UpdateClient
) : AppUpdater {
    private val uriHandler = DesktopUriHandler()

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
        uriHandler.openUri(latest.htmlUrl)
        return null
    }

    override fun updateTo(release: AppRelease): Flow<UpdateProgress>? {
        uriHandler.openUri(release.htmlUrl)
        return null
    }
}


private class DesktopUriHandler : UriHandler {
    override fun openUri(uri: String) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(URI(uri))
        } else when (DesktopPlatform.Current) {
            DesktopPlatform.Linux -> Runtime.getRuntime().exec(arrayOf("xdg-open", URI(uri).toString()))
            DesktopPlatform.Windows, DesktopPlatform.MacOS ->
                throw UnsupportedOperationException(
                    "AWT doesn't support the BROWSE action on ${DesktopPlatform.Current}"
                )
            DesktopPlatform.Unknown ->
                throw UnsupportedOperationException("AWT doesn't support ${DesktopPlatform.Current}")
        }
    }
}
