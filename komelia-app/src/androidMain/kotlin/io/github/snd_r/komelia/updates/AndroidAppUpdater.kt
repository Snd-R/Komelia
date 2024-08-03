package io.github.snd_r.komelia.updates

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION
import android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL
import android.os.Build
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.io.readByteArray
import java.util.concurrent.atomic.AtomicBoolean

class AndroidAppUpdater(
    private val githubClient: UpdateClient,
    private val context: Context,
) : AppUpdater {
    private var inProgress = AtomicBoolean(false)

    override suspend fun getReleases(): List<AppRelease> {
        return githubClient.getKomeliaReleases().map { it.toAppRelease() }
    }

    override suspend fun updateToLatest(): Flow<UpdateProgress>? {
        val latest = githubClient.getKomeliaLatestRelease().toAppRelease()
        return updateTo(latest)
    }

    override fun updateTo(release: AppRelease): Flow<UpdateProgress>? {
        if (!inProgress.compareAndSet(false, true)) return null
        if (release.assetUrl == null) return null

        return flow {
            emit(UpdateProgress(0, 0))
            val sessionParams = PackageInstaller.SessionParams(MODE_FULL_INSTALL)
            val packageInstaller = context.packageManager.packageInstaller
            val sessionId = packageInstaller.createSession(sessionParams)
            val session = packageInstaller.openSession(sessionId)

            githubClient.streamFile(release.assetUrl) { response -> streamToSession(response, session) }

            val receiverIntent = Intent(context, PackageInstallerStatusReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val receiverPendingIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, flags)
            session.commit(receiverPendingIntent.intentSender)
            session.close()
            inProgress.set(false)
        }
    }

    private suspend fun FlowCollector<UpdateProgress>.streamToSession(
        response: HttpResponse,
        session: PackageInstaller.Session
    ) {
        val length = response.headers["Content-Length"]?.toLong() ?: 0L
        emit(UpdateProgress(length, 0))
        val channel = response.bodyAsChannel().counted()
        val sessionStream = session.openWrite("komelia", 0, -1)
        sessionStream.buffered().use { bufferedSessionStream ->
            while (!channel.isClosedForRead) {

                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.exhausted()) {
                    val bytes = packet.readByteArray()
                    bufferedSessionStream.write(bytes)
                }
                emit(UpdateProgress(length, channel.totalBytesRead))
            }
            bufferedSessionStream.flush()
            session.fsync(sessionStream)
        }
    }

    private fun GithubRelease.toAppRelease(): AppRelease {
        val asset = assets.firstOrNull { it.name.endsWith(".apk") }

        return AppRelease(
            version = AppVersion.fromString(tagName),
            publishDate = publishedAt,
            releaseNotesBody = body.replace("\r", ""),
            htmlUrl = htmlUrl,
            assetName = asset?.name,
            assetUrl = asset?.browserDownloadUrl
        )
    }
}

class PackageInstallerStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (confirmationIntent != null) {
                    context.startActivity(confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }

            else -> {}
        }
    }
}
