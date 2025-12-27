package snd.komelia.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class SettingsStateWrapper<T>(
    settings: T,
    private val saveSettings: suspend (T) -> Unit
) {
    private val _state: MutableStateFlow<T> = MutableStateFlow(settings)
    val state = _state.asStateFlow()

    inline fun <R> mapState(crossinline transform: suspend (value: T) -> R): Flow<R> {
        return state.map(transform).distinctUntilChanged()
    }

    suspend fun transform(transform: suspend (settings: T) -> T) {
        val transformed = transform(_state.value)
        saveSettings(transformed)
        _state.value = transformed
    }
}
