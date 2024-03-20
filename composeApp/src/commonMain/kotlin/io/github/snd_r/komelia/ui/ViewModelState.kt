package io.github.snd_r.komelia.ui

sealed interface LoadState<out T> {
    data object Uninitialized : LoadState<Nothing>
    data object Loading : LoadState<Nothing>
    data class Error(val exception: Throwable) : LoadState<Nothing>
    data class Success<T>(val value: T) : LoadState<T>
}

