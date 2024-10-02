package io.github.snd_r.komelia.ui.common

data class StateHolder<T>(
    val value: T,
    val setValue: (T) -> Unit,
    val errorMessage: String? = null
)

data class OptionsStateHolder<T>(
    val value: T,
    val options: List<T>,
    val onValueChange: (T) -> Unit
)

data class StateListWithOptions<T>(
    val value: List<T>,
    val options: List<T>,
    val onValueChange: (List<T>) -> Unit
)
