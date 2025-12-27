package snd.komelia.updates

import kotlinx.coroutines.flow.Flow
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider

interface OnnxRuntimeInstaller {
     fun install(provider: OnnxRuntimeExecutionProvider): Flow<UpdateProgress>
}