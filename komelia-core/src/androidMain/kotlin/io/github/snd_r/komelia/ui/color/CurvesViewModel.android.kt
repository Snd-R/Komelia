package io.github.snd_r.komelia.ui.color

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate

actual fun <T> Flow<T>.debounceImageTransforms(): Flow<T> = this.conflate()