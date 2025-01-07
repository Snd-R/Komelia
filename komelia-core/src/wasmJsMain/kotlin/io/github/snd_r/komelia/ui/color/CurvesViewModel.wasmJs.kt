package io.github.snd_r.komelia.ui.color

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
actual fun <T> Flow<T>.debounceImageTransforms(): Flow<T> = this.debounce(100)