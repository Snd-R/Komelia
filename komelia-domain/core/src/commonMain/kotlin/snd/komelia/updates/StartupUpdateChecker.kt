package snd.komelia.updates

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import snd.komelia.settings.CommonSettingsRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

private val logger = KotlinLogging.logger {}

class StartupUpdateChecker(
    private val updater: AppUpdater,
    private val settings: CommonSettingsRepository,
    private val releaseFlow: MutableStateFlow<List<AppRelease>>,
) {
    private val updateScope = CoroutineScope(Dispatchers.Default)
    val downloadProgress = MutableStateFlow<UpdateProgress?>(null)

    suspend fun checkForUpdates(): AppRelease? {
        try {
            val checkForUpdates = settings.getCheckForUpdatesOnStartup().first()
            if (!checkForUpdates) return null

            val lastChecked = settings.getLastUpdateCheckTimestamp().first()
            if (lastChecked != null && lastChecked > Clock.System.now().minus(24.hours)) return null

            val releases = updater.getReleases()
            val latest = releases.first()
            releaseFlow.value = releases
            settings.putLastUpdateCheckTimestamp(Clock.System.now())
            settings.putLastCheckedReleaseVersion(latest.version)

            if (AppVersion.current >= latest.version) return null
            if (settings.getDismissedVersion().first() == AppVersion.current) return null

            return latest
        } catch (e: Exception) {
            logger.catching(e)
            return null
        }
    }

    suspend fun onUpdate(release: AppRelease) {
        updater.updateTo(release)
            ?.conflate()
            ?.onCompletion { downloadProgress.value = null }
            ?.onEach { downloadProgress.value = it }
            ?.launchIn(updateScope)
        settings.putDismissedVersion(release.version)
    }

    fun onUpdateCancel() {
        updateScope.coroutineContext.cancelChildren()
    }

    suspend fun onUpdateDismiss(release: AppRelease) {
        settings.putDismissedVersion(release.version)
    }

}