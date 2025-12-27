package snd.komelia.image

actual fun availableReduceKernels() = listOf(
    ReduceKernel.LANCZOS3,
    ReduceKernel.LANCZOS2,
    ReduceKernel.MITCHELL,
//    ReduceKernel.NEAREST,
//    ReduceKernel.LINEAR,
//    ReduceKernel.CUBIC,
//    ReduceKernel.MKS2013,
//    ReduceKernel.MKS2021,
)
