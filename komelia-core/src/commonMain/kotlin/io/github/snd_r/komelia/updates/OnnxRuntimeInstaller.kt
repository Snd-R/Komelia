package io.github.snd_r.komelia.updates

import kotlinx.coroutines.flow.Flow
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider

interface OnnxRuntimeInstaller {
    suspend fun install(provider: OnnxRuntimeExecutionProvider): Flow<UpdateProgress>
}