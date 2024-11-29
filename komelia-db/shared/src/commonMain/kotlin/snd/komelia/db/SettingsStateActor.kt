package snd.komelia.db

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class SettingsStateActor<T>(
    settings: T,
    private val saveSettings: suspend (T) -> Unit
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _state: MutableStateFlow<T> = MutableStateFlow(settings)
    private val updateFlow = MutableSharedFlow<TransformRequest<T>>(1, 0, SUSPEND)

    val state = _state.asStateFlow()

    init {
        updateFlow.onEach { handleTransform(it) }.launchIn(scope)
    }

    inline fun <R> mapState(crossinline transform: suspend (value: T) -> R): Flow<R> {
        return state.map(transform).distinctUntilChanged()
    }

    suspend fun transform(transform: suspend (settings: T) -> T) {
        val ack = CompletableDeferred<T>()
        updateFlow.emit(TransformRequest(ack, transform))
        ack.await()
    }

    private suspend fun handleTransform(request: TransformRequest<T>) {
        request.ack.completeWith(
            runCatching {
                val transformed = request.transform(_state.value)
                saveSettings(transformed)
                _state.value = transformed

                transformed
            }
        )
    }

    data class TransformRequest<T>(
        val ack: CompletableDeferred<T>,
        val transform: suspend (settings: T) -> T,
    )
}
