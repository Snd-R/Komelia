package io.github.snd_r.komelia.ui.settings.updates

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.updates.AppRelease
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.AppVersion
import io.github.snd_r.komelia.updates.UpdateProgress
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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import snd.settings.CommonSettingsRepository

class AppUpdatesViewModel(
    val releases: MutableStateFlow<List<AppRelease>>,
    val updater: AppUpdater,
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

        latestVersion.value = settings.getLastCheckedReleaseVersion().first()
        checkForUpdatesOnStartup.value = settings.getCheckForUpdatesOnStartup().first()
        lastUpdateCheck.value = settings.getLastUpdateCheckTimestamp().first()
    }

    fun checkForUpdates() {
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