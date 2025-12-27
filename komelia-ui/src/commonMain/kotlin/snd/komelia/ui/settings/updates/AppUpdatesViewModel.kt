package snd.komelia.ui.settings.updates

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.updates.AppRelease
import snd.komelia.updates.AppUpdater
import snd.komelia.updates.AppVersion
import snd.komelia.updates.UpdateProgress
import kotlin.time.Clock
import kotlin.time.Instant

class AppUpdatesViewModel(
    val releases: MutableStateFlow<List<AppRelease>>,
    val updater: AppUpdater?,
    val settings: CommonSettingsRepository,
    val notifications: AppNotifications
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    val latestVersion = MutableStateFlow<AppVersion?>(null)
    val checkForUpdatesOnStartup = MutableStateFlow(false)
    val lastUpdateCheck = MutableStateFlow<Instant?>(null)

    private val updateScope = CoroutineScope(Dispatchers.Default)
    val downloadProgress = MutableStateFlow<UpdateProgress?>(null)

    suspend fun initialize() {
        if (state.value != Uninitialized) return
        if (updater == null) {
            mutableState.value = LoadState.Error(IllegalStateException("Updater is not initialized"))
            return
        }

        latestVersion.value = settings.getLastCheckedReleaseVersion().first()
        checkForUpdatesOnStartup.value = settings.getCheckForUpdatesOnStartup().first()
        lastUpdateCheck.value = settings.getLastUpdateCheckTimestamp().first()
    }

    fun checkForUpdates() {
        val updater = requireNotNull(updater)
        notifications.runCatchingToNotifications(screenModelScope) {
            mutableState.value = LoadState.Loading

            val releases = updater.getReleases()
            this.releases.value = releases
            if (releases.isEmpty()) return@runCatchingToNotifications

            val latestRelease = releases.first()
            latestVersion.value = latestRelease.version

            val now = Clock.System.now()
            settings.putLastUpdateCheckTimestamp(now)
            lastUpdateCheck.value = now

            settings.putLastCheckedReleaseVersion(latestRelease.version)

            mutableState.value = LoadState.Success(Unit)
        }
    }

    fun onUpdate() {
        val updater = requireNotNull(updater)
        notifications.runCatchingToNotifications(screenModelScope) {
            val cachedRelease = releases.value.firstOrNull()
            val progress = if (cachedRelease != null) updater.updateTo(cachedRelease)
            else updater.updateToLatest()

            progress
                ?.conflate()
                ?.onCompletion { downloadProgress.value = null }
                ?.onEach { downloadProgress.value = it }
                ?.launchIn(updateScope)
        }
    }

    fun onUpdateCancel() {
        updateScope.coroutineContext.cancelChildren()
    }

    fun onCheckForUpdatesOnStartupChange(check: Boolean) {
        this.checkForUpdatesOnStartup.value = check
        screenModelScope.launch { settings.putCheckForUpdatesOnStartup(check) }
    }
}