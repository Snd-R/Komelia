package io.github.snd_r.komelia.ui.settings.server

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import snd.komga.client.actuator.KomgaActuatorClient
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.common.PatchValue
import snd.komga.client.common.patch
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryClient
import snd.komga.client.settings.KomgaSettings
import snd.komga.client.settings.KomgaSettingsClient
import snd.komga.client.settings.KomgaSettingsUpdateRequest
import snd.komga.client.settings.KomgaThumbnailSize
import snd.komga.client.task.KomgaTaskClient

class ServerSettingsViewModel(
    private val appNotifications: AppNotifications,
    private val settingsClient: KomgaSettingsClient,
    private val bookClient: KomgaBookClient,

    private val libraryClient: KomgaLibraryClient,
    private val libraries: StateFlow<List<KomgaLibrary>>,
    private val taskClient: KomgaTaskClient,
    private val actuatorClient: KomgaActuatorClient,
) : ScreenModel {

    val currentSettings = MutableStateFlow<KomgaSettings?>(null)

    val deleteEmptyCollections = MutableStateFlow(false)
    val deleteEmptyReadLists = MutableStateFlow(false)

    val rememberMeDurationDays = MutableStateFlow<Int?>(365)
    val rememberMeDurationDaysValidationMessage = MutableStateFlow<String?>(null)

    val renewRememberMeKey = MutableStateFlow(false)

    val taskPoolSize = MutableStateFlow<Int?>(1)
    val taskPoolSizeValidationMessage = MutableStateFlow<String?>(null)

    val serverPort = MutableStateFlow<Int?>(null)
    val configServerPort = MutableStateFlow<Int?>(25600)
    val serverContextPath = MutableStateFlow<String?>(null)

    val thumbnailSize = MutableStateFlow(KomgaThumbnailSize.DEFAULT)
    private val regenerateThumbnails = MutableStateFlow(ThumbnailRegenerateOption.NONE)

    val isChanged = MutableStateFlow(false)

    init {
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications {
                loadSettings()
            }
        }
    }


    fun regenerateThumbnails(forBiggerResultOnly: Boolean) {
        screenModelScope.launch {
            bookClient.regenerateThumbnails(forBiggerResultOnly)
        }
    }

    fun updateSettings() {
        screenModelScope.launch {
            val currentSettings = currentSettings.value ?: return@launch
            val request = KomgaSettingsUpdateRequest(
                deleteEmptyCollections = patch(currentSettings.deleteEmptyCollections, deleteEmptyCollections.value),
                deleteEmptyReadLists = patch(currentSettings.deleteEmptyReadLists, deleteEmptyReadLists.value),
                rememberMeDurationDays = patch(currentSettings.rememberMeDurationDays, rememberMeDurationDays.value),
                renewRememberMeKey = if (renewRememberMeKey.value) PatchValue.Some(true) else PatchValue.Unset,
                thumbnailSize = patch(currentSettings.thumbnailSize, thumbnailSize.value),
                taskPoolSize = patch(currentSettings.taskPoolSize, taskPoolSize.value),
                serverPort = patch(currentSettings.serverPort.databaseSource, serverPort.value),
                serverContextPath = patch(currentSettings.serverContextPath.databaseSource, serverContextPath.value)
            )
            appNotifications.runCatchingToNotifications {
                settingsClient.updateSettings(request)
                appNotifications.add(AppNotification.Success("Updated Server Settings"))
                loadSettings()
            }
        }
    }

    fun resetChanges() {
        val settings = requireNotNull(currentSettings.value)
        deleteEmptyCollections.value = settings.deleteEmptyCollections
        deleteEmptyReadLists.value = settings.deleteEmptyReadLists
        rememberMeDurationDays.value = settings.rememberMeDurationDays
        thumbnailSize.value = settings.thumbnailSize
        taskPoolSize.value = settings.taskPoolSize
        serverPort.value = settings.serverPort.databaseSource
        configServerPort.value = settings.serverPort.configurationSource
        serverContextPath.value = settings.serverContextPath.databaseSource
        isChanged.value = false
    }

    fun onDeleteEmptyCollectionsChange(deleteEmptyCollections: Boolean) {
        isChanged.value = true
        this.deleteEmptyCollections.value = deleteEmptyCollections
    }

    fun onDeleteEmptyReadListsChange(deleteEmptyReadLists: Boolean) {
        isChanged.value = true
        this.deleteEmptyReadLists.value = deleteEmptyReadLists
    }

    fun onRenewRememberMeKeyChange(renewRememberMeKey: Boolean) {
        isChanged.value = true
        this.renewRememberMeKey.value = renewRememberMeKey

    }

    fun onServerPortChange(serverPort: Int?) {
        isChanged.value = true
        this.serverPort.value = serverPort
    }

    fun onServerContextPathChange(serverContextPath: String?) {
        isChanged.value = true
        this.serverContextPath.value = serverContextPath
    }

    fun onRememberMeDurationDaysChange(days: Int?) {
        isChanged.value = true
        if (days == null) rememberMeDurationDaysValidationMessage.value = "Required"
        else rememberMeDurationDaysValidationMessage.value = null
        this.rememberMeDurationDays.value = days

    }

    fun onTaskPoolSizeChange(taskPoolSize: Int?) {
        isChanged.value = true
        if (taskPoolSize == null) taskPoolSizeValidationMessage.value = "Required"
        else taskPoolSizeValidationMessage.value = null
        this.taskPoolSize.value = taskPoolSize
    }

    fun onThumbnailSizeChange(size: KomgaThumbnailSize) {
        isChanged.value = true
        thumbnailSize.value = size
        regenerateThumbnails.value = ThumbnailRegenerateOption.ONLY_IF_BIGGER
    }

    private suspend fun loadSettings() {
        currentSettings.value = settingsClient.getSettings()
        resetChanges()
    }


    fun onScanAllLibraries(deep: Boolean) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            libraries.value.forEach { libraryClient.scan(it.id, deep) }
            appNotifications.add(AppNotification.Success("Launched scan for all libraries"))
        }
    }

    fun onEmptyTrashForAllLibraries() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            libraries.value.forEach { libraryClient.emptyTrash(it.id) }
            appNotifications.add(AppNotification.Success("Emptied trash for all libraries"))
        }
    }

    fun onCancelAllTasks() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            val cancelledTasks = taskClient.emptyTaskQueue()

            if (cancelledTasks == 0)
                appNotifications.add(AppNotification.Normal("No tasks to cancel"))
            else
                appNotifications.add(AppNotification.Success("$cancelledTasks tasks cancelled"))
        }
    }

    fun onShutDown() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            actuatorClient.shutdown()
        }

    }

    enum class ThumbnailRegenerateOption {
        NONE,
        ONLY_IF_BIGGER,
        ALL
    }
}