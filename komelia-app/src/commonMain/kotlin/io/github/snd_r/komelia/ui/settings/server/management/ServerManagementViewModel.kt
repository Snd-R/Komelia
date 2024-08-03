package io.github.snd_r.komelia.ui.settings.server.management

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotification.Success
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komga.actuator.KomgaActuatorClient
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryClient
import io.github.snd_r.komga.task.KomgaTaskClient
import kotlinx.coroutines.flow.StateFlow

class ServerManagementViewModel(
    private val appNotifications: AppNotifications,
    private val libraryClient: KomgaLibraryClient,
    private val libraries: StateFlow<List<KomgaLibrary>>,
    private val taskClient: KomgaTaskClient,
    private val actuatorClient: KomgaActuatorClient,
) : ScreenModel {

    fun onScanAllLibraries(deep: Boolean) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            libraries.value.forEach { libraryClient.scan(it.id, deep) }
            appNotifications.add(Success("Launched scan for all libraries"))
        }
    }

    fun onEmptyTrashForAllLibraries() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            libraries.value.forEach { libraryClient.emptyTrash(it.id) }
            appNotifications.add(Success("Emptied trash for all libraries"))
        }
    }

    fun onCancelAllTasks() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            val cancelledTasks = taskClient.emptyTaskQueue()

            if (cancelledTasks == 0)
                appNotifications.add(AppNotification.Normal("No tasks to cancel"))
            else
                appNotifications.add(Success("$cancelledTasks tasks cancelled"))
        }
    }

    fun onShutDown() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            actuatorClient.shutdown()
        }

    }

}