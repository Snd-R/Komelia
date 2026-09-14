package snd.komelia.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsStateWrapper<T>(
    settings: T,
    private val saveSettings: suspend (T) -> Unit
) {
    private val _state: MutableStateFlow<T> = MutableStateFlow(settings)
    val state = _state.asStateFlow()
    private val persistScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    inline fun <R> mapState(crossinline transform: suspend (value: T) -> R): Flow<R> {
        return state.map(transform).distinctUntilChanged()
    }

    // Updates in-memory state immediately and fires a background DB save.
    // Use this when the caller cannot wait for the save (e.g. non-suspend context).
    fun update(transform: (T) -> T) {
        val transformed = transform(_state.value)
        _state.value = transformed
        persistScope.launch { saveSettings(transformed) }
    }

    suspend fun transform(transform: suspend (settings: T) -> T) {
        val transformed = transform(_state.value)
        _state.value = transformed
        saveSettings(transformed)
    }
}
