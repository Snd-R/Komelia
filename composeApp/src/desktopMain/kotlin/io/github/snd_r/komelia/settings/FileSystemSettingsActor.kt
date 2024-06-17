package io.github.snd_r.komelia.settings

import com.akuleshov7.ktoml.TomlIndentation.NONE
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.file.TomlFileReader
import com.akuleshov7.ktoml.file.TomlFileWriter
import dev.dirs.ProjectDirectories
import io.github.snd_r.komelia.settings.ActorMessage.Transform
import io.github.snd_r.komelia.settings.State.Initialized
import io.github.snd_r.komelia.settings.State.UnInitialized
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.notExists

sealed class State {
    data object UnInitialized : State()
    data class Initialized(val settings: AppSettings) : State()
}

@OptIn(ObsoleteCoroutinesApi::class)
class FileSystemSettingsActor {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val state: MutableStateFlow<State> = MutableStateFlow(UnInitialized)

    private val configFile = Path.of(
        ProjectDirectories.from("io.github.snd-r.komelia", "", "Komelia").configDir
    ).resolve("komelia.toml")
    private val tomlFileReader = TomlFileReader(inputConfig = TomlInputConfig.compliant(ignoreUnknownNames = true))
    private val tomlFileWriter = TomlFileWriter(outputConfig = TomlOutputConfig.compliant(indentation = NONE))

    private val queue = scope.actor<ActorMessage> {
        for (message in channel) {
            when (message) {
                is ActorMessage.Read -> handleRead(message)
                is ActorMessage.Update -> handleUpdate(message)
                is Transform -> handleTransform(message)

            }
        }
    }

    fun getState(): StateFlow<AppSettings> {
        val currentSettings = when (val settings = state.value) {
            is Initialized -> settings.settings
            is UnInitialized -> throw IllegalStateException("settings state is not initialized")
        }

        return state.map {
            when (val settings = it) {
                is Initialized -> settings.settings
                is UnInitialized -> throw IllegalStateException("settings state is not initialized")
            }
        }.stateIn(scope, SharingStarted.Eagerly, currentSettings)
    }

    suspend fun send(msg: ActorMessage) {
        queue.send(msg)
    }

    suspend fun transform(transform: suspend (settings: AppSettings) -> AppSettings) {
        val ack = CompletableDeferred<AppSettings>()
        queue.send(Transform(ack, transform))
        ack.await()
    }

    private fun handleRead(read: ActorMessage.Read) {
        read.ack.completeWith(
            runCatching {
                val settings =
                    if (configFile.notExists()) AppSettings()
                    else tomlFileReader.decodeFromFile(AppSettings.serializer(), configFile.absolutePathString())

                state.value = Initialized(settings)
                return@runCatching
            }
        )
    }

    private fun handleUpdate(update: ActorMessage.Update) {
        update.ack.completeWith(
            runCatching {
                val currentState = state.value
                check(currentState is Initialized)
                writeData(update.settings)
                state.value = Initialized(update.settings)
                update.settings
            }
        )
    }

    private suspend fun handleTransform(transform: Transform) {
        transform.ack.completeWith(
            runCatching {
                val currentState = state.value
                check(currentState is Initialized)

                val transformed = transform.transform(currentState.settings)
                writeData(transformed)
                state.value = Initialized(transformed)
                transformed
            }
        )
    }

    private fun writeData(settings: AppSettings) {
        Files.createDirectories(configFile.parent)
        tomlFileWriter.encodeToFile(
            AppSettings.serializer(),
            settings,
            configFile.absolutePathString()
        )
    }
}

sealed class ActorMessage {
    data class Read(
        val ack: CompletableDeferred<Unit>
    ) : ActorMessage()

    data class Update(
        val settings: AppSettings,
        val ack: CompletableDeferred<AppSettings>,
    ) : ActorMessage()

    data class Transform(
        val ack: CompletableDeferred<AppSettings>,
        val transform: suspend (settings: AppSettings) -> AppSettings,
    ) : ActorMessage()
}