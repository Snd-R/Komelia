package snd.komelia.db.settings

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SettingsActor(
    settings: AppSettings,
    private val saveSettings: (AppSettings) -> Unit
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _state: MutableStateFlow<AppSettings> = MutableStateFlow(settings)
    private val updateFlow = MutableSharedFlow<TransformRequest>(1, 0, SUSPEND)

    val state = _state.asStateFlow()

    init {
        updateFlow.onEach { handleTransform(it) }.launchIn(scope)
    }

    suspend fun transform(transform: suspend (settings: AppSettings) -> AppSettings) {
        val ack = CompletableDeferred<AppSettings>()
        updateFlow.emit(TransformRequest(ack, transform))
        ack.await()
    }

    private suspend fun handleTransform(request: TransformRequest) {
        request.ack.completeWith(
            runCatching {
                val transformed = request.transform(_state.value)
                saveSettings(transformed)
                _state.value = transformed

                transformed
            }
        )
    }

    data class TransformRequest(
        val ack: CompletableDeferred<AppSettings>,
        val transform: suspend (settings: AppSettings) -> AppSettings,
    )
}
