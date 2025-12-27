package snd.komelia.onnxruntime

enum class OnnxRuntimeExecutionProvider(val nativeOrdinal: Int) {
    TENSOR_RT(0),
    CUDA(1),
    ROCm(2),
    DirectML(3),
    CPU(4),
    WEBGPU(5),
}
